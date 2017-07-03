package fr.cnes.regards.modules.crawler.service.consumer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;

/**
 * An abstract data object saver manager
 * @author oroussel
 */
public abstract class AbstractDataObjectBulkSaver {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataObjectBulkSaver.class);

    /**
     * Set of data objects to usually save
     */
    private HashSet<DataObject> toSaveObjects;

    /**
     * Callable used to save data
     */
    private SaveDataObjectsCallable saveDataObjectsCallable;

    /**
     * Saving task
     */
    private Future<Void> saveBulkTask = null;

    /**
     * Executor used to create tasks
     */
    private ExecutorService executor;

    /**
     * Dataset if onlyu used for loggin prurpose
     */
    private long datasetId;

    protected AbstractDataObjectBulkSaver(SaveDataObjectsCallable saveDataObjectsCallable, ExecutorService executor,
            HashSet<DataObject> toSaveObjects, long datasetId) {
        this.saveDataObjectsCallable = saveDataObjectsCallable;
        this.executor = executor;
        this.toSaveObjects = toSaveObjects;
        this.datasetId = datasetId;
    }

    protected void addDataObject(DataObject object) {
        this.toSaveObjects.add(object);
    }

    protected boolean needToSave() {
        return (toSaveObjects.size() == IEsRepository.BULK_SIZE);
    }

    /**
     * Ask for set saving
     */
    protected void saveSet() {
        this.waitForEndOfTask();
        LOGGER.info("Launching Saving of {} data objects task (dataset {})...", toSaveObjects.size(), datasetId);
        // Give a clone of data objects to save set
        saveDataObjectsCallable.setSet((Set<DataObject>) toSaveObjects.clone());
        // Clear data objects to save set
        toSaveObjects.clear();
        // Add task to thread pool executor
        saveBulkTask = executor.submit(saveDataObjectsCallable);

    }

    /**
     * Waiting for currently task to end
     */
    protected void waitForEndOfTask() {
        if (saveBulkTask != null) {
            LOGGER.info("Waiting for previous task to end (dataset {})...", datasetId);
            try {
                saveBulkTask.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(String.format("Unable to save data objects (dataset %d)", datasetId), e);
            }
        }
    }

    /**
     * To directly save remaining data objects
     */
    public void finalSave() {
        this.waitForEndOfTask();
        if (!toSaveObjects.isEmpty()) {
            try {
                // Directly call on current thread without doing a clone
                saveDataObjectsCallable.setSet(toSaveObjects);
                saveDataObjectsCallable.call();
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to save data objects (dataset %d)", datasetId), e);
            }
        }
    }
}
