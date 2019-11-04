package fr.cnes.regards.modules.model.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;

/**
 * AMQP event for fragment deletion
 *
 * @author Sylvain VISSIERE-GUERINET
 */
@Event(target = Target.ALL)
public class FragmentDeletedEvent implements ISubscribable {

    /**
     * The deleted fragment name
     */
    private String fragmentName;

    /**
     * Default constructor
     */
    public FragmentDeletedEvent() {
    }

    /**
     * Constructor setting the parameter as attribute
     * @param fragmentName
     */
    public FragmentDeletedEvent(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    /**
     * @return the fragment name
     */
    public String getFragmentName() {
        return fragmentName;
    }

    /**
     * Set the fragment name
     * @param fragmentName
     */
    public void setFragmentName(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        FragmentDeletedEvent that = (FragmentDeletedEvent) o;

        return fragmentName != null ? fragmentName.equals(that.fragmentName) : that.fragmentName == null;
    }

    @Override
    public int hashCode() {
        return fragmentName != null ? fragmentName.hashCode() : 0;
    }
}
