/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.domain;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@Table(name = "T_COLLECTION")
public class Collection extends AbstractEntity {

    private static final String COLLECTION_TYPE = "Collection";

    /**
     * description
     */
    @Column
    protected String description;

    /**
     * name
     */
    @NotNull
    @Column
    protected String name;

    /**
     * list of other entities that this collection contains
     */
    @OneToMany(mappedBy = "id", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
            CascadeType.REFRESH })
    protected List<AbstractEntity> content;

    /**
     * list of collection containing this one
     */
    @OneToMany
    protected List<Collection> parents;

    public Collection() {
        super();
    }

    /**
     * contructor for children
     *
     * @param pModel
     * @param pEntity
     */
    public Collection(Model pModel, String pEntity, String pDescription, String pName) {
        super(pModel, pEntity);
        description = pDescription;
        name = pName;
    }

    public Collection(Model pModel, String pDescription, String pName) {
        super(pModel, COLLECTION_TYPE);
        description = pDescription;
        name = pName;
    }

    public Collection(String pSipId, Model pModel, String pDescription, String pName) {
        this(pModel, pDescription, pName);
        sipId = pSipId;
        ipId = new UniformResourceName(OAISIdentifier.AIP, COLLECTION_TYPE, JWTService.getActualTenant(),
                UUID.nameUUIDFromBytes(sipId.getBytes()), 1);
    }

    public Collection(Long pId, Model pModel, String pDescription, String pName) {
        this(pModel, pDescription, pName);
        id = pId;
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
