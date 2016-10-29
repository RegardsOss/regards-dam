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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

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
@Table(name = "T_MODEL_ATT")
@SequenceGenerator(name = "modelAttSequence", initialValue = 1, sequenceName = "SEQ_MODEL_ATT")
public class ModelAttribute implements Comparable<ModelAttribute>, IIdentifiable<Long> {

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
    @JoinColumn(name = "attribute_id", foreignKey = @ForeignKey(name = "ATTRIBUTE_ID_FK"))
    private AttributeModel attribute;

    /**
     * Whether this attribute in computed or not
     */
    // TODO link to a calculation plugin
    private Boolean isCalculated = Boolean.FALSE;

    /**
     * Position (allows to sort attribute in model)
     */
    private Short pos = 0;

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

    public Boolean getIsCalculated() {
        return isCalculated;
    }

    public void setIsCalculated(Boolean pIsCalculated) {
        isCalculated = pIsCalculated;
    }

    public Short getPos() {
        return pos;
    }

    public void setPos(Short pPosition) {
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
}
