/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * FIXME: class initialized for dataaccess requirement, to be really implemented
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@DiscriminatorValue("DATASET")
public class DataSet extends AbstractLinkEntity {

    /**
     * Quality mark
     */
    private int score;

    public DataSet(Model pModel, UniformResourceName pIpId) {
        super(pModel, pIpId, EntityType.DATASET);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int pScore) {
        score = pScore;
    }

}
