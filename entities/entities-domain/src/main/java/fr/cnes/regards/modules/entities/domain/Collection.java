/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
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
    protected List<AbstractLinkEntity> links = new ArrayList<>();

    public Collection(Model pModel, String pTenant, String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, pTenant, UUID.randomUUID(), 1),
              pLabel);
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

    public void addLink(AbstractLinkEntity pLink) {
        links.add(pLink);
    }

    @Override
    public String getType() {
        return EntityType.COLLECTION.toString();
    }
}
