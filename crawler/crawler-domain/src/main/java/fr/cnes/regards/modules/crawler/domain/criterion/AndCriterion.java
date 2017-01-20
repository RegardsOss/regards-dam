package fr.cnes.regards.modules.crawler.domain.criterion;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Defines a list of criterions (logicaly AND)
 * @author oroussel
 */
public class AndCriterion implements ICriterion {

    /**
     * Criterions
     */
    private List<ICriterion> criterions = new ArrayList<>();

    protected AndCriterion() {
    }

    protected AndCriterion(ICriterion... pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    protected AndCriterion(Iterable<ICriterion> pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    public void addCriterion(ICriterion pCriterion) {
        this.criterions.add(pCriterion);
    }

    public List<ICriterion> getCriterions() {
        return criterions;
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitAndCriterion(this);
    }
}
