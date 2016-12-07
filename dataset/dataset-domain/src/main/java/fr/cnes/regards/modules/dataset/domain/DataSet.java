/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataset.domain;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 *
 * FIXME: class initialized for dataaccess requirement, to be really implemented
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class DataSet {

    private UniformResourceName urn;

    private int score;

    public UniformResourceName getUrn() {
        return urn;
    }

    public void setUrn(UniformResourceName pUrn) {
        urn = pUrn;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int pScore) {
        score = pScore;
    }

}
