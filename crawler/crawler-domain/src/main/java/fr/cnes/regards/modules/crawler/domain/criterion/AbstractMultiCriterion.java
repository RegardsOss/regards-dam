package fr.cnes.regards.modules.crawler.domain.criterion;

import java.util.ArrayList;
import java.util.List;

public class AbstractMultiCriterion {

    /**
     * Criterions
     */
    protected List<ICriterion> criterions = new ArrayList<>();

    public AbstractMultiCriterion() {
        super();
    }

    public void addCriterion(ICriterion pCriterion) {
        this.criterions.add(pCriterion);
    }

    public List<ICriterion> getCriterions() {
        return criterions;
    }

}