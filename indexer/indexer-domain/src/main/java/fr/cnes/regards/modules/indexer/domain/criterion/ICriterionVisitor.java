package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * ICriterion visitor
 * @param <T> return type of all methods
 * @author oroussel
 */
public interface ICriterionVisitor<T> {

    T visitEmptyCriterion(EmptyCriterion criterion);

    T visitAndCriterion(AbstractMultiCriterion criterion);

    T visitOrCriterion(AbstractMultiCriterion criterion);

    T visitNotCriterion(NotCriterion criterion);

    T visitStringMatchCriterion(StringMatchCriterion criterion);

    T visitStringMatchAnyCriterion(StringMatchAnyCriterion criterion);

    T visitIntMatchCriterion(IntMatchCriterion criterion);

    T visitLongMatchCriterion(LongMatchCriterion criterion);

    T visitDateMatchCriterion(DateMatchCriterion criterion);

    <U extends Comparable<? super U>> T visitRangeCriterion(RangeCriterion<U> criterion);

    T visitDateRangeCriterion(DateRangeCriterion criterion);

    T visitBooleanMatchCriterion(BooleanMatchCriterion criterion);

    T visitPolygonCriterion(PolygonCriterion criterion);

    T visitCircleCriterion(CircleCriterion criterion);

    T visitFieldExistsCriterion(FieldExistsCriterion criterion);
}
