/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import javax.persistence.Entity;

import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 */
@Entity
public class DataSet extends AbstractLinkEntity {

    /**
     * Quality mark
     */
    private int score;

    /**
     * this list contains IDs of any plugin associated to this DataSet, for example: ids of Converters, Services,
     * Filters
     */
    private List<Long> pluginIds;

    private DataSource dataSource;

    private ICriterion subsettingClause;

    public DataSet(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int pScore) {
        score = pScore;
    }

    @Override
    public String getType() {
        return EntityType.DATASET.toString();
    }

}
