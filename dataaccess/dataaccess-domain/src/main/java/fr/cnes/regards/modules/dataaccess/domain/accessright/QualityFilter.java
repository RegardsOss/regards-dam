/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
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
