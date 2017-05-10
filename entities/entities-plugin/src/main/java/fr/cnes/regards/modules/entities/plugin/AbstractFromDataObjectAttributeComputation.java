/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * Implementation of {@link IComputedAttribute} plugin interface.
 * @param <R> type of the attribute value
 * @author Sylvain Vissiere-Guerinet
 */
public abstract class AbstractFromDataObjectAttributeComputation<R> implements IComputedAttribute<Dataset, R> {

    private IEsRepository esRepo;

    protected IAttributeModelService attModelService;

    protected IRuntimeTenantResolver tenantResolver;

    protected AttributeModel attributeToCompute;

    protected R result;

    @Override
    public R getResult() {
        return result;
    }

    /**
     * Each of those beans cannot be wired into the abstract so they have to be autowired into plugin implementation and
     * given thanks to this method. Doing so fully initialize the abstraction.
     */
    protected void initAbstract(IEsRepository esRepo, IAttributeModelService attModelService,
            IRuntimeTenantResolver tenantResolver) {
        this.esRepo = esRepo;
        this.attModelService = attModelService;
        this.tenantResolver = tenantResolver;
    }

    /**
     * @param dataset dataset on which the attribute, once computed, will be added. This allows us to know which
     * DataObject should be used.
     */
    @Override
    public void compute(Dataset dataset) {
        // create the search
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(tenantResolver.getTenant(),
                EntityType.DATA.toString(), DataObject.class);
        esRepo.searchAll(searchKey, this.doCompute(), dataset.getSubsettingClause());
    }

    @Override
    public AttributeModel getAttributeToCompute() {
        return attributeToCompute;
    }

    protected abstract Consumer<DataObject> doCompute();

    /**
     * Extract the properties of the fragment in which the attribute
     * {@link AbstractFromDataObjectAttributeComputation#attributeToCompute} is located
     */
    protected Set<AbstractAttribute<?>> extractProperties(DataObject datum) { //NOSONAR
        if (attributeToCompute.getFragment().isDefaultFragment()) {
            // the attribute is in the default fragment so it has at the root level of properties
            return datum.getProperties().stream().filter(p -> !(p instanceof ObjectAttribute))
                    .collect(Collectors.toSet());
        } else {
            // the attribute is in a fragment so we have to be get the right fragment(ObjectAttribute) before we
            // can access the attribute
            return datum.getProperties().stream()
                    .filter(p -> (p instanceof ObjectAttribute)
                            && p.getName().equals(attributeToCompute.getFragment().getName()))
                    .flatMap(fragment -> ((ObjectAttribute) fragment).getValue().stream()).collect(Collectors.toSet());
        }
    }

}
