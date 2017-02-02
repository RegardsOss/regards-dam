/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Entity
public class Collection extends AbstractLinkEntity { // NOSONAR

    /**
     * Links are direct access collections and datasets retrieved from tags (see {@link AbstractEntity})
     */
    @OneToMany(mappedBy = "id",
            cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected List<AbstractLinkEntity> links;

    public Collection(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public Collection() {
        this(null, null, null);
    }

    public List<AbstractLinkEntity> getLinks() {
        return links;
    }

    public void setLinks(List<AbstractLinkEntity> pLinks) {
        links = pLinks;
    }

    @Override
    public String getType() {
        return EntityType.COLLECTION.toString();
    }
}
