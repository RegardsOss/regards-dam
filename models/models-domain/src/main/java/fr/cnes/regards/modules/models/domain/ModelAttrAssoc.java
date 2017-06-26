/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.validation.ComputedAttribute;
import fr.cnes.regards.modules.models.domain.xml.IXmlisable;
import fr.cnes.regards.modules.models.schema.Attribute;
import fr.cnes.regards.modules.models.schema.Computation;
import fr.cnes.regards.modules.models.schema.NoParamPluginType;
import fr.cnes.regards.modules.models.schema.ParamPluginType;

/**
 * Model - attribute association.</br>
 * A ModelAttrAssoc is linked to a {@link Model}.<br/>
 * It contains the reference to a global {@link AttributeModel} and adds the capacity to define if its value is set
 * manually or calculated through a calculation plugin.<<br/>
 * Thus, a same {@link AttributeModel} may be linked to different model and can either be set manually or calculated
 * depending on the model.
 *
 * @author msordi
 */
@Entity
@Table(name = "ta_model_att_att", uniqueConstraints = @UniqueConstraint(name = "uk_model_att_att_id_model_id",
        columnNames = { "attribute_id", "model_id" }))
@SequenceGenerator(name = "modelAttSequence", initialValue = 1, sequenceName = "seq_model_att")
@ComputedAttribute
public class ModelAttrAssoc implements Comparable<ModelAttrAssoc>, IIdentifiable<Long>, IXmlisable<Attribute> {

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modelAttSequence")
    private Long id;

    /**
     * Common attribute model
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "attribute_id", foreignKey = @ForeignKey(name = "fk_attribute_id"), updatable = false)
    @NotNull
    private AttributeModel attribute;

    @OneToOne
    @JoinColumn(name = "compute_conf_id", foreignKey = @ForeignKey(name = "fk_plugin_id"))
    private PluginConfiguration computationConf;

    /**
     * Related model
     */
    @ManyToOne
    @JoinColumn(name = "model_id", foreignKey = @ForeignKey(name = "fk_model_id"), nullable = false, updatable = false)
    private Model model;

    /**
     * Position (allows to sort attribute in model)
     */
    @Column
    private Integer pos = 0;

    public ModelAttrAssoc() { // NOSONAR
        super();
    }

    public ModelAttrAssoc(AttributeModel pAttributeModel, Model pModel, Integer pPosition, Boolean pIsCalculated) {// NOSONAR
        attribute = pAttributeModel;
        model = pModel;
        pos = pPosition;
    }

    public ModelAttrAssoc(AttributeModel pAttributeModel, Model pModel, Integer pPosition) {// NOSONAR
        this(pAttributeModel, pModel, pPosition, Boolean.FALSE);
    }

    public ModelAttrAssoc(AttributeModel pAttributeModel, Model pModel) {// NOSONAR
        this(pAttributeModel, pModel, 0, Boolean.FALSE);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public AttributeModel getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeModel pAttribute) {
        attribute = pAttribute;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pPosition) {
        pos = pPosition;
    }

    @Override
    public int compareTo(ModelAttrAssoc pOther) {
        return pos - pOther.getPos();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ModelAttrAssoc other = (ModelAttrAssoc) obj;
        if (attribute == null) {
            if (other.attribute != null) {
                return false;
            }
        } else if (!attribute.equals(other.attribute)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((attribute == null) ? 0 : attribute.hashCode());
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model pModel) {
        model = pModel;
    }

    public ComputationMode getMode() {
        return (computationConf == null) ? ComputationMode.GIVEN : ComputationMode.COMPUTED;
    }

    @Override
    public Attribute toXml() {
        final Attribute xmlAtt = attribute.toXml();
        if (computationConf != null) {
            Computation computation = new Computation();
            computation.setLabel(computationConf.getLabel());
            // Cyclic dependency between entities plugin and models-domain
            // TODO : Find a good idea to avoid this shit
            // Count plugin are really something different from others, lets treat them apart
            String pluginClassName=computationConf.getPluginClassName();
            if(pluginClassName.equals("fr.cnes.regards.modules.entities.plugin.CountPlugin")) {
                computation.setCount(new NoParamPluginType());
            } else {
                // For plugins which are calculated according to a data object property, lets set the parameters and then the type
                ParamPluginType paramPluginType = new ParamPluginType();
                paramPluginType.setParameterAttributeName(
                        computationConf.getParameter("parameterAttributeName").getValue());
                String parameterAttributeFragmentName = computationConf.getParameter("parameterAttributeFragmentName").getValue();
                if (parameterAttributeFragmentName != null) {
                    paramPluginType.setParameterAttributeFragmentName(parameterAttributeFragmentName);
                }
                switch (pluginClassName) {
                    case "fr.cnes.regards.modules.entities.plugin.IntSumComputePlugin":
                    case "fr.cnes.regards.modules.entities.plugin.LongSumComputePlugin":
                        computation.setSumCompute(paramPluginType);
                        break;
                    case "fr.cnes.regards.modules.entities.plugin.MaxDateComputePlugin":
                        computation.setMinCompute(paramPluginType);
                        break;
                    case "fr.cnes.regards.modules.entities.plugin.MinDateComputePlugin":
                        computation.setMaxCompute(paramPluginType);
                        break;
                }
            }
            xmlAtt.setComputation(computation);
        }
        return xmlAtt;
    }

    @Override
    public void fromXml(Attribute pXmlElement) {
        // Manage base attribute
        final AttributeModel attModel = new AttributeModel();
        attModel.fromXml(pXmlElement);
        setAttribute(attModel);

    }

    public PluginConfiguration getComputationConf() {
        return computationConf;
    }

    public void setComputationConf(PluginConfiguration pComputationConf) {
        computationConf = pComputationConf;
    }

}
