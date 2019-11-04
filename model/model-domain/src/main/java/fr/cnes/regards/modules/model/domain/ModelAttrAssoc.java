/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.model.domain;

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
import fr.cnes.regards.framework.module.manager.ConfigIgnore;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.PluginParamType;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.schema.Attribute;
import fr.cnes.regards.modules.model.domain.schema.Computation;
import fr.cnes.regards.modules.model.domain.schema.NoParamPluginType;
import fr.cnes.regards.modules.model.domain.schema.ParamPluginType;
import fr.cnes.regards.modules.model.domain.validator.ComputedAttribute;

/**
 * Model - attribute association.</br>
 * A ModelAttrAssoc is linked to a {@link Model}.<br/>
 * It contains the reference to a global {@link AttributeModel} and adds the capacity to define if its value is set
 * manually or calculated through a calculation plugin.<<br/>
 * Thus, a same {@link AttributeModel} may be linked to different model and can either be set manually or calculated
 * depending on the model.
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
    @ConfigIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modelAttSequence")
    private Long id;

    /**
     * Common attribute model
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "attribute_id", foreignKey = @ForeignKey(name = "fk_attribute_id"), updatable = false)
    @NotNull
    private AttributeModel attribute;

    /**
     * The computation plugin configuration
     */
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

    /**
     * Constructor
     * @param pAttributeModel {@link Model}
     * @param pModel
     * @param pPosition
     * @param pIsCalculated
     */
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

    public Model getModel() {
        return model;
    }

    public void setModel(Model pModel) {
        model = pModel;
    }

    public ComputationMode getMode() {
        return computationConf == null ? ComputationMode.GIVEN : ComputationMode.COMPUTED;
    }

    @Override
    public Attribute toXml() {
        final Attribute xmlAtt = attribute.toXml();
        if (computationConf != null) {
            Computation computation = new Computation();
            computation.setLabel(computationConf.getLabel());
            // Cyclic dependency between dam-plugin and dam-domain
            // TODO : Find a good idea to avoid this shit
            // Count plugin are really something different from others, lets treat them apart
            String pluginClassName = computationConf.getPluginClassName();
            if ("fr.cnes.regards.modules.dam.plugin.entities.CountPlugin".equals(pluginClassName)) {
                computation.setCount(new NoParamPluginType());
            } else {
                // For plugins which are calculated according to a data object property, let's set the parameters and
                // then the type
                ParamPluginType paramPluginType = new ParamPluginType();
                String parameterAttributeName = (String) computationConf.getParameter("parameterAttributeName")
                        .getValue();
                if (parameterAttributeName.matches("^\"[^\"]*\"$")) {
                    parameterAttributeName = parameterAttributeName.substring(1, parameterAttributeName.length() - 1);
                }
                paramPluginType.setParameterAttributeName(parameterAttributeName);
                IPluginParam paramAttrFragment = computationConf.getParameter("parameterAttributeFragmentName");
                if (paramAttrFragment != null && paramAttrFragment.getType() == PluginParamType.STRING) {
                    String paramAttrFragmentName = (String) paramAttrFragment.getValue();
                    if (paramAttrFragmentName != null) {
                        if (paramAttrFragmentName.matches("^\"[^\"]*\"$")) {
                            paramAttrFragmentName = paramAttrFragmentName.substring(1,
                                                                                    paramAttrFragmentName.length() - 1);
                        }
                        paramPluginType.setParameterAttributeFragmentName(paramAttrFragmentName);
                    }
                }
                switch (pluginClassName) {
                    case "fr.cnes.regards.modules.dam.plugin.entities.IntSumComputePlugin":
                    case "fr.cnes.regards.modules.dam.plugin.entities.LongSumComputePlugin":
                        computation.setSumCompute(paramPluginType);
                        break;
                    case "fr.cnes.regards.modules.dam.plugin.entities.MaxDateComputePlugin":
                        computation.setMaxCompute(paramPluginType);
                        break;
                    case "fr.cnes.regards.modules.dam.plugin.entities.MinDateComputePlugin":
                        computation.setMinCompute(paramPluginType);
                        break;
                    default:
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

    /**
     * @return the computation plugin configuration
     */
    public PluginConfiguration getComputationConf() {
        return computationConf;
    }

    /**
     * Set the computation plugin configuration
     * @param pComputationConf  {@link PluginConfiguration}
     */
    public void setComputationConf(PluginConfiguration pComputationConf) {
        computationConf = pComputationConf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModelAttrAssoc that = (ModelAttrAssoc) o;

        if (attribute != null ? !attribute.equals(that.attribute) : that.attribute != null) {
            return false;
        }
        return model != null ? model.equals(that.model) : that.model == null;
    }

    @Override
    public int hashCode() {
        int result = attribute != null ? attribute.hashCode() : 0;
        result = 31 * result + (model != null ? model.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ModelAttrAssoc{" + "id=" + id + ", attribute=" + attribute + ", computationConf=" + computationConf
                + ", model=" + model + '}';
    }
}
