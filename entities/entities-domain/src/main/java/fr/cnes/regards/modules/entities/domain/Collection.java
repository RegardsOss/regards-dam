/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@DiscriminatorValue("COLLECTION")
public class Collection extends AbstractLinkEntity { // NOSONAR

    /**
     * list of other entities that this collection contains
     */
    @OneToMany(mappedBy = "id", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
            CascadeType.REFRESH })
    protected List<AbstractLinkEntity> links;

    /*
     * public Collection() { // NOSONAR super(null, EntityType.COLLECTION); }
     */

    public Collection(Model pModel, UniformResourceName pIpId) {
        super(pModel, pIpId, EntityType.COLLECTION);
    }

    public List<AbstractLinkEntity> getLinks() {
        return links;
    }

    public void setLinks(List<AbstractLinkEntity> pLinks) {
        links = pLinks;
    }
}
