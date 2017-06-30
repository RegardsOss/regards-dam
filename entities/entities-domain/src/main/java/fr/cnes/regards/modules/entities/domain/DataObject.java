/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * A DataObject is created by a DataSource when an external database is ingested.
 *
 * @author lmieulet
 * @author Marc Sordi
 * @author oroussel
 */
public class DataObject extends AbstractDataEntity {

    /**
     * This field permits to identify which datasource provides it
     */
    private String dataSourceId;

    /**
     * Denormalization : allows to retrieve dataobjects related to models (i.e. types) of dataset
     */
    private final Set<Long> datasetModelIds = new HashSet<>();

    public DataObject(Model pModel, String pTenant, String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, pTenant, UUID.randomUUID(), 1),
              pLabel);
    }

    public DataObject() {
        this(null, null, null);
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String pDataSourceId) {
        this.dataSourceId = pDataSourceId;
    }

    public Set<Long> getDatasetModelIds() {
        return datasetModelIds;
    }

    @Override
    public String getType() {
        return EntityType.DATA.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object pObject) {
        return super.equals(pObject);
    }
}
