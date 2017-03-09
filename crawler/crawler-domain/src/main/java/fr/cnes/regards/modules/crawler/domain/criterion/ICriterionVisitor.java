package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * ICriterion visitor
 * @param <T> return type of all methods
 * @author oroussel
 */
public interface ICriterionVisitor<T> {

    T visitEmptyCriterion(EmptyCriterion pCriterion);

    T visitAndCriterion(AbstractMultiCriterion pCriterion);

    T visitOrCriterion(AbstractMultiCriterion pCriterion);

    T visitNotCriterion(NotCriterion pCriterion);

    T visitStringMatchCriterion(StringMatchCriterion pCriterion);

    T visitStringMatchAnyCriterion(StringMatchAnyCriterion pCriterion);

    T visitIntMatchCriterion(IntMatchCriterion pCriterion);

    T visitLongMatchCriterion(LongMatchCriterion pCriterion);

    <U extends Comparable<? super U>> T visitRangeCriterion(RangeCriterion<U> pCriterion);

    T visitDateRangeCriterion(DateRangeCriterion pCriterion);

    T visitBooleanMatchCriterion(BooleanMatchCriterion pCriterion);
}
