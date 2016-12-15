/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataset.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import fr.cnes.regards.modules.collections.domain.Collection;
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

    private static final String DATASET_TYPE = "DataSet";

    private int score;

    public DataSet(Model pModel, String pDescription, String pName) {
        super(pModel, DATASET_TYPE, pDescription, pName);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int pScore) {
        score = pScore;
    }

}
