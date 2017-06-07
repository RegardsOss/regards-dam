/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.base.Strings;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * Implementation of {@link IComputedAttribute} plugin interface.
 * @param <R> type of the result attribute value
 * @author Sylvain Vissiere-Guerinet
 */
public abstract class AbstractDataObjectComputePlugin<R> implements IComputedAttribute<Dataset, R> {

    private IEsRepository esRepo;

    protected IAttributeModelRepository attModelRepos;

    private IRuntimeTenantResolver tenantResolver;

    private AttributeModel parameterAttribute;

    private AttributeModel attributeToCompute;

    protected R result;

    @Override
    public R getResult() {
        return result;
    }

    /**
     * Each of those beans cannot be wired into the abstract so they have to be autowired into plugin implementation and
     * given thanks to this method. Doing so fully initialize the abstraction.
     */
    protected void initAbstract(IEsRepository esRepo, IAttributeModelRepository attModelRepos,
            IRuntimeTenantResolver tenantResolver) {
        this.esRepo = esRepo;
        this.attModelRepos = attModelRepos;
        this.tenantResolver = tenantResolver;
    }

    protected void init(String attributeToComputeName, String attributeToComputeFragmentName,
            String parameterAttributeName, String parameterAttributeFragmentName) {
        attributeToCompute = attModelRepos.findByNameAndFragmentName(attributeToComputeName, (Strings.isNullOrEmpty(
                attributeToComputeFragmentName) ? Fragment.getDefaultName() : attributeToComputeFragmentName));
        if (attributeToCompute == null) {
            if (!Strings.isNullOrEmpty(attributeToComputeFragmentName)) {
                throw new EntityNotFoundException(
                        String.format("Cannot find computed attribute '%s'.'%s'", attributeToComputeFragmentName,
                                      attributeToComputeName));
            } else {
                throw new EntityNotFoundException(
                        String.format("Cannot find computed attribute '%s'", attributeToComputeName));
            }
        }
        parameterAttribute = attModelRepos.findByNameAndFragmentName(parameterAttributeName, (Strings.isNullOrEmpty(
                parameterAttributeFragmentName) ? Fragment.getDefaultName() : parameterAttributeFragmentName));
        if (parameterAttribute == null) {
            if (!Strings.isNullOrEmpty(parameterAttributeFragmentName)) {
                throw new EntityNotFoundException(
                        String.format("Cannot find parameter attribute '%s'.'%s'", parameterAttributeFragmentName,
                                      parameterAttributeName));
            } else {
                throw new EntityNotFoundException(
                        String.format("Cannot find parameter attribute '%s'", parameterAttributeName));
            }
        }

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
     * Extract the property of which name and eventually fragment name are given
     */
    protected Optional<AbstractAttribute<?>> extractProperty(DataObject object) { //NOSONAR
        if (parameterAttribute.getFragment().isDefaultFragment()) {
            // the attribute is in the default fragment so it has at the root level of properties
            return object.getProperties().stream().filter(p -> p.getName().equals(parameterAttribute.getName()))
                    .findFirst();
        }
        // the attribute is in a fragment so :
        // filter the fragment property then filter the right property on fragment properties
        return object.getProperties().stream().filter(p -> (p instanceof ObjectAttribute) && p.getName()
                .equals(parameterAttribute.getFragment().getName())).limit(1) // Only one fragment with searched name
                .flatMap(fragment -> ((ObjectAttribute) fragment).getValue().stream())
                .filter(p -> p.getName().equals(parameterAttribute.getName())).findFirst();
    }

}
