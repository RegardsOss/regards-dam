package fr.cnes.regards.modules.crawler.service.consumer;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.DataAccessLevel;
import fr.cnes.regards.modules.dataaccess.service.IAccessRightService;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Data object accumulator and multi thread Elasticsearch bulk saver
 */
public class DataObjectUpdater extends AbstractDataObjectBulkSaver implements Consumer<DataObject> {

    private final String datasetIpId;

    private final Set<String> groups;

    private final OffsetDateTime updateDate;

    private final Long datasetModelId;

    private Table<String, String, Boolean> groupDatasetDataAccessTable = HashBasedTable.create();

    public DataObjectUpdater(Dataset dataset, OffsetDateTime updateDate, HashSet<DataObject> toSaveObjects,
            SaveDataObjectsCallable saveDataObjectsCallable, ExecutorService executor,
            IAccessRightService accessRightService) {
        super(saveDataObjectsCallable, executor, toSaveObjects, dataset.getId());
        this.datasetIpId = dataset.getIpId().toString();
        this.groups = dataset.getMetadata().getDataObjectsGroups();
        this.datasetModelId = dataset.getModel().getId();
        this.updateDate = updateDate;
        this.groups.forEach(g -> {
            boolean dataAccessRight = false;
            try {
                Optional<AccessRight> accessRightOpt = accessRightService
                        .retrieveAccessRight(g, UniformResourceName.fromString(datasetIpId));
                dataAccessRight = accessRightOpt.isPresent()
                        && accessRightOpt.get().getDataAccessRight().getDataAccessLevel()
                        == DataAccessLevel.INHERITED_ACCESS;
            } catch (EntityNotFoundException e) {
                dataAccessRight = false;
            }
            groupDatasetDataAccessTable.put(g, datasetIpId, dataAccessRight);
        });
    }

    @Override
    public void accept(DataObject object) {
        // reset groups and modelIds for this datasetIpId
        object.getMetadata().removeDatasetIpId(datasetIpId);
        object.getTags().add(datasetIpId);
        // set current groups and modelIds on metadata for this datasetIpId
        groups.forEach(group -> object.getMetadata()
                .addGroup(group, datasetIpId, groupDatasetDataAccessTable.get(group, datasetIpId)));
        object.getMetadata().addModelId(datasetModelId, datasetIpId);
        // update groups from metadata
        object.setGroups(object.getMetadata().getGroups());
        // update modelIds from metadata
        object.setDatasetModelIds(object.getMetadata().getModelIds());
        object.setLastUpdate(updateDate);
        super.addDataObject(object);
        if (super.needToSave()) {
            super.saveSet();
        }
    }

}