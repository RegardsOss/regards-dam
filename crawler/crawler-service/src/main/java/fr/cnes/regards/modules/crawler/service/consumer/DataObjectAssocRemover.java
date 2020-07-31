package fr.cnes.regards.modules.crawler.service.consumer;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;

/**
 * Consumer removing association between dataset and data object
 * @author oroussel
 */
public class DataObjectAssocRemover extends AbstractDataObjectBulkSaver implements Consumer<DataObject> {

    private final String datasetIpId;

    private final OffsetDateTime updateDate;

    public DataObjectAssocRemover(Dataset dataset, OffsetDateTime updateDate, HashSet<DataObject> toSaveObjects,
            SaveDataObjectsCallable saveDataObjectsCallable, ExecutorService executor, Integer maxBulkSize) {
        super(saveDataObjectsCallable, executor, toSaveObjects, dataset.getId(), maxBulkSize);
        this.datasetIpId = dataset.getIpId().toString();
        this.updateDate = updateDate;
    }

    @Override
    public void accept(DataObject object) {
        object.removeTags(Arrays.asList(datasetIpId));
        object.getMetadata().removeDatasetIpId(datasetIpId);
        object.setGroups(object.getMetadata().getGroups());
        object.setDatasetModelNames(object.getMetadata().getModelNames());
        object.setLastUpdate(updateDate);
        super.addDataObject(object);
        if (super.needToSave()) {
            super.saveSet();
        }
    }
}
