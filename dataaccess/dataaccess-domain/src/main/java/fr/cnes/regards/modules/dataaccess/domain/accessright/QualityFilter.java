/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Embeddable
public class QualityFilter {

    @Min(0)
    @Max(10)
    @Column(name = "max_score")
    private int maxScore;

    @Min(0)
    @Max(10)
    @Column(name = "min_score")
    private int minScore;

    @Column(length = 30, name = "quality_level")
    @Enumerated(EnumType.STRING)
    private QualityLevel qualityLevel;

    @SuppressWarnings("unused")
    private QualityFilter() {

    }

    public QualityFilter(int pMaxScore, int pMinScore, QualityLevel pQualityLevel) {
        super();
        maxScore = pMaxScore;
        minScore = pMinScore;
        qualityLevel = pQualityLevel;
    }

    /*
     * public Long getId() { return id; }
     * 
     * public void setId(Long pId) { id = pId; }
     */

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int pMaxScore) {
        maxScore = pMaxScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(int pMinScore) {
        minScore = pMinScore;
    }

    public QualityLevel getQualityLevel() {
        return qualityLevel;
    }

    public void setQualityLevel(QualityLevel pQualityLevel) {
        qualityLevel = pQualityLevel;
    }

}
