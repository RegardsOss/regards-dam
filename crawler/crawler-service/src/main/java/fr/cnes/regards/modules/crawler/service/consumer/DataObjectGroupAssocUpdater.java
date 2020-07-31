package fr.cnes.regards.modules.crawler.service.consumer;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;

public class DataObjectGroupAssocUpdater extends AbstractDataObjectBulkSaver implements Consumer<DataObject> {

    private final String datasetIpId;

    private final String groupName;

    private final OffsetDateTime updateDate;

    public DataObjectGroupAssocUpdater(Dataset dataset, OffsetDateTime updateDate, HashSet<DataObject> toSaveObjects,
            SaveDataObjectsCallable saveDataObjectsCallable, ExecutorService executor, String groupName,
            Integer maxBulkSize) {
        super(saveDataObjectsCallable, executor, toSaveObjects, dataset.getId(), maxBulkSize);
        this.datasetIpId = dataset.getIpId().toString();
        this.updateDate = updateDate;
        this.groupName = groupName;
    }

    @Override
    public void accept(DataObject object) {
        object.getMetadata().addGroup(groupName, datasetIpId, true);
        object.setGroups(object.getMetadata().getGroups());
        object.setLastUpdate(updateDate);
        super.addDataObject(object);
        if (super.needToSave()) {
            super.saveSet();
        }
    }

}
