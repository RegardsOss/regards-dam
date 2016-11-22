/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.domain;

import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@Table(name = "T_COLLECTION")
@AssociationOverrides({ @AssociationOverride(
        name = "model", joinColumns = @JoinColumn(name = "model_id",
                foreignKey = @ForeignKey(name = "FK_COLLECTION_MODEL_ID"), nullable = false, updatable = false),
        foreignKey = @ForeignKey(name = "FK_COLLECTION_MODEL_ID")) })
public class Collection extends AbstractEntity {

    /**
     * model that this entity is respecting
     */
    @ManyToOne
    @JoinColumn(name = "model_id", foreignKey = @ForeignKey(name = "FK_ENTITY_MODEL_ID"), nullable = false,
            updatable = false)
    protected Model model;

    /**
     * description
     */
    @Column
    private String description;

    /**
     * name
     */
    @NotNull
    @Column
    private String name;

    /**
     * list of other entities that this collection contains
     */
    @OneToMany(mappedBy = "id",
            cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private List<AbstractEntity> content;

    /**
     * list of collection containing this one
     */
    @OneToMany
    private List<Collection> parents;

    public Collection() {
        super();
    }

    public Collection(Long pId, Model pModel, String pDescription, String pName) {
        super(pModel, pId);
        description = pDescription;
        name = pName;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(String pDescription) {
        description = pDescription;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name = pName;
    }

    @Override
    public boolean equals(Object pObj) {
        return (pObj instanceof Collection) && ((Collection) pObj).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public List<AbstractEntity> getContent() {
        return content;
    }

    public void setContent(List<AbstractEntity> pContent) {
        content = pContent;
    }

    public List<Collection> getParents() {
        return parents;
    }

    public void setParents(List<Collection> pParents) {
        parents = pParents;
    }
}
