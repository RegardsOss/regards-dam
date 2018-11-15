package fr.cnes.regards.modules.crawler.service.consumer;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.metadata.DatasetMetadata.DataObjectGroup;

/**
 * Data object accumulator and multi thread Elasticsearch bulk saver
 */
public class DataObjectUpdater extends AbstractDataObjectBulkSaver implements Consumer<DataObject> {

    private final String datasetIpId;

    private final Map<String, DataObjectGroup> groupsMap;

    private final OffsetDateTime updateDate;

    private final Long datasetModelId;

    public DataObjectUpdater(Dataset dataset, OffsetDateTime updateDate, HashSet<DataObject> toSaveObjects,
            SaveDataObjectsCallable saveDataObjectsCallable, ExecutorService executor) {
        super(saveDataObjectsCallable, executor, toSaveObjects, dataset.getId());
        this.datasetIpId = dataset.getIpId().toString();
        this.groupsMap = dataset.getMetadata().getDataObjectsGroupsMap();
        this.datasetModelId = dataset.getModel().getId();
        this.updateDate = updateDate;
    }

    @Override
    public void accept(DataObject object) {
        // reset groupsMap and modelIds for this datasetIpId
        object.getMetadata().removeDatasetIpId(datasetIpId);
        object.addTags(datasetIpId);
        // set current groups with no plugin access filter from groupsMap on metadata for this datasetIpId
        // Calcul of group access with plugins are done in an other step.
        // This step only associate group to dataobjets of dataset with no filter. All objets of the dataset have the same groups.
        for (DataObjectGroup group : groupsMap.values()) {
            if (group.getMetaDataObjectAccessFilterPluginId() == null) {
                object.getMetadata().addGroup(group.getGroupName(), datasetIpId, group.getDataObjectAccess());
            }
        }
        // set current modelIds on metadata for this datasetIpId
        object.getMetadata().addModelId(datasetModelId, datasetIpId);
        // update groupsMap from metadata
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