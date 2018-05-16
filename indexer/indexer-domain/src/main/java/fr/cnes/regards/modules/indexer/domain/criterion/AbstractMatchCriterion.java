package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * A match criterion specifies how a value have to be matched
 * @param <T> type of value
 * @author oroussel
 */
public abstract class AbstractMatchCriterion<T> extends AbstractPropertyCriterion implements ICriterion {

    /**
     * Matching type
     */
    protected MatchType type;

    /**
     * Value to be matched
     */
    protected T value;

    private AbstractMatchCriterion(String name) {
        super(name);
    }

    public AbstractMatchCriterion(String name, MatchType type, T value) {
        this(name);
        this.type = type;
        this.value = value;
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((type == null) ? 0 : type.hashCode());
        result = (prime * result) + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        AbstractMatchCriterion<?> other = (AbstractMatchCriterion<?>) o;
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
