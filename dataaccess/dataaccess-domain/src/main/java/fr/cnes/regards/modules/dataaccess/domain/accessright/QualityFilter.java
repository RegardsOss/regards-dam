/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
public class QualityFilter {

    @Id
    @SequenceGenerator(name = "QualityFilterSequence", initialValue = 1, sequenceName = "SEQ_QUALITY_FILTER")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "QualityFilterSequence")
    private Long id;

    @Min(0)
    @Max(10)
    private int maxScore;

    @Min(0)
    @Max(10)
    private int minScore;

    @Enumerated
    private QualityLevel qualityLevel;

    public QualityFilter(int pMaxScore, int pMinScore, QualityLevel pQualityLevel) {
        super();
        maxScore = pMaxScore;
        minScore = pMinScore;
        qualityLevel = pQualityLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

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
