/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

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
@Table(name = "T_DATA_SET")
public class DataSet extends Collection {

    private int score;

    public DataSet(Model pModel, String pDescription, String pName) {
        super(pModel, EntityType.DATASET, pDescription, pName);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int pScore) {
        score = pScore;
    }

}
