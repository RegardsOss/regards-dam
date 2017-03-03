package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * A match criterion specifies how a value have to be matched
 * @param <T> type of value
 * @author oroussel
 */
public abstract class AbstractMatchCriterion<T> extends AbstractPropertyCriterion implements ICriterion {

    /**
     * Matching type
     */
    private MatchType type;

    /**
     * Value to be matched
     */
    private T value;

    private AbstractMatchCriterion(String pName) {
        super(pName);
    }

    public AbstractMatchCriterion(String pName, MatchType pType, T pValue) {
        this(pName);
        type = pType;
        value = pValue;
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType pType) {
        type = pType;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T pValue) {
        value = pValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        // CHECKSTYLE:OFF
        result = (prime * result) + ((type == null) ? 0 : type.hashCode());
        result = (prime * result) + ((value == null) ? 0 : value.hashCode());
        // CHECKSTYLE:ON
        return result;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!super.equals(pObj)) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        AbstractMatchCriterion<?> other = (AbstractMatchCriterion<?>) pObj;
        if (type != other.type) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
