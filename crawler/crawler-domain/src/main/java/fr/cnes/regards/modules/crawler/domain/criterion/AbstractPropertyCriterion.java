package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * Property criterion. Provides a property name
 * @author oroussel
 */
public abstract class AbstractPropertyCriterion implements ICriterion {

    /**
     * Concerned property name
     */
    protected String name;

    public AbstractPropertyCriterion(String pName) {
        super();
        name = pName;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractPropertyCriterion other = (AbstractPropertyCriterion) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else
            if (!name.equals(other.name)) {
                return false;
            }
        return true;
    }

}
