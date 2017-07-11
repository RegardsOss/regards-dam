package fr.cnes.regards.modules.crawler.service.consumer;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Consumer removing association between dataset and data object
 * @author oroussel
 */
public class DataObjectAssocRemover extends AbstractDataObjectBulkSaver implements Consumer<DataObject> {
    private String datasetIpId;

    private OffsetDateTime updateDate;

    public DataObjectAssocRemover(Dataset dataset, OffsetDateTime updateDate, HashSet<DataObject> toSaveObjects,
            SaveDataObjectsCallable saveDataObjectsCallable, ExecutorService executor) {
        super(saveDataObjectsCallable, executor, toSaveObjects, dataset.getId());
        this.datasetIpId = dataset.getIpId().toString();
        this.updateDate = updateDate;
    }

    @Override
    public void accept(DataObject object) {
        object.getTags().remove(datasetIpId);
        object.getMetadata().removeDatasetIpId(datasetIpId);
        object.setGroups(object.getMetadata().getGroups());
        object.setDatasetModelIds(object.getMetadata().getModelIds());
        object.setLastUpdate(updateDate);
        super.addDataObject(object);
        if (super.needToSave()) {
            super.saveSet();
        }
    }
}