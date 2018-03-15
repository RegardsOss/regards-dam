package fr.cnes.regards.modules.crawler.service.consumer;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Data object accumulator and multi thread Elasticsearch bulk saver
 */
public class DataObjectUpdater extends AbstractDataObjectBulkSaver implements Consumer<DataObject> {

    private final String datasetIpId;

    private final Map<String, Boolean> groupsMap;

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
        object.getTags().add(datasetIpId);
        // set current groupsMap and modelIds on metadata for this datasetIpId
        groupsMap.forEach((group, dataGranted) -> object.getMetadata().addGroup(group, datasetIpId, dataGranted));
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