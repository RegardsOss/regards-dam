/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

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
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.xml.IXmlisable;
import fr.cnes.regards.modules.models.schema.Attribute;

/**
 *
 * A {@link ModelAttribute} is linked to a {@link Model}.<br/>
 * It contains the reference to a global {@link AttributeModel} and adds the capacity to define if its value is set
 * manually or calculated threw a calculation plugin.<<br/>
 * Thus, a same {@link AttributeModel} may be linked to different model and can either be set manually or calculated
 * depending the model.
 *
 * @author msordi
 *
 */
@Entity
@Table(name = "T_MODEL_ATT", uniqueConstraints = @UniqueConstraint(columnNames = { "attribute_id", "model_id" }))
@SequenceGenerator(name = "modelAttSequence", initialValue = 1, sequenceName = "SEQ_MODEL_ATT")
public class ModelAttribute implements Comparable<ModelAttribute>, IIdentifiable<Long>, IXmlisable<Attribute> {

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modelAttSequence")
    private Long id;

    /**
     * Common attribute model
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "attribute_id", foreignKey = @ForeignKey(name = "ATTRIBUTE_ID_FK"), updatable = false)
    @NotNull
    private AttributeModel attribute;

    /**
     * Whether this attribute in computed or not
     */
    // TODO link to a calculation plugin
    @NotNull
    private ComputationMode mode = ComputationMode.GIVEN;

    /**
     * Related model
     */
    @ManyToOne
    @JoinColumn(name = "model_id", foreignKey = @ForeignKey(name = "MODEL_ID_FK"), nullable = false, updatable = false)
    private Model model;

    /**
     * Position (allows to sort attribute in model)
     */
    private Integer pos = 0;

    public ModelAttribute() {
        super();
    }

    public ModelAttribute(AttributeModel pAttributeModel, Model pModel, Integer pPosition, Boolean pIsCalculated) {
        attribute = pAttributeModel;
        this.model = pModel;
        this.pos = pPosition;
        this.mode = ComputationMode.GIVEN;
    }

    public ModelAttribute(AttributeModel pAttributeModel, Model pModel, Integer pPosition) {
        this(pAttributeModel, pModel, pPosition, Boolean.FALSE);
    }

    public ModelAttribute(AttributeModel pAttributeModel, Model pModel) {
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
    public int compareTo(ModelAttribute pOther) {
        return this.pos - pOther.getPos();
    }

    @Override
    public boolean equals(Object pObj) {
        Boolean result = Boolean.FALSE;
        if (pObj instanceof ModelAttribute) {
            final ModelAttribute modelAtt = (ModelAttribute) pObj;
            result = modelAtt.getAttribute().equals(this.getAttribute());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return this.getAttribute().hashCode();
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model pModel) {
        model = pModel;
    }

    public ComputationMode getMode() {
        return mode;
    }

    public void setMode(ComputationMode pMode) {
        mode = pMode;
    }

    @Override
    public Attribute toXml() {
        final Attribute xmlAtt = attribute.toXml();
        xmlAtt.setComputationMode(mode.toString());
        return xmlAtt;
    }

    @Override
    public void fromXml(Attribute pXmlElement) {
        // TODO Auto-generated method stub
    }
}
