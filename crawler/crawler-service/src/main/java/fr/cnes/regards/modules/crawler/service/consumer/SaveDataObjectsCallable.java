package fr.cnes.regards.modules.crawler.service.consumer;

import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;

/**
 * Callable used to parallelize data objects bulk save into Elasticsearch
 * @author oroussel
 */
public class SaveDataObjectsCallable implements Callable<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SaveDataObjectsCallable.class);

    /**
     * Tenant resolver needed to force tenant because ot multi-threading
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Elasticsearch repository
     */
    private final IEsRepository esRepos;

    /**
     * Current tenant
     */
    private final String tenant;

    /**
     * Set of objects to save
     */
    private Set<DataObject> set;

    /**
     * dataset id (only used for logging purpose)
     */
    private final long datasetId;

    public SaveDataObjectsCallable(IRuntimeTenantResolver runtimeTenantResolver, IEsRepository esRepos, String tenant,
            long datasetId) {
        this.runtimeTenantResolver = runtimeTenantResolver;
        this.esRepos = esRepos;
        this.tenant = tenant;
        this.datasetId = datasetId;
    }

    /**
     * Setting set of data objects to be saved later
     * @param set ideally, a copy or clone of set (we don't know when saving will be called)
     */
    public void setSet(Set<DataObject> set) {
        this.set = set;
    }

    @Override
    public Void call() throws Exception {
        if ((set != null) && !set.isEmpty()) {
            LOGGER.info("Saving {} data objects (dataset {})...", set.size(), datasetId);
            runtimeTenantResolver.forceTenant(tenant);
            esRepos.saveBulk(tenant, set);
            LOGGER.info("...data objects saved");
        }
        return null;
    }
}