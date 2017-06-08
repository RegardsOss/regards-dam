package fr.cnes.regards.modules.models.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;

/**
 * @author Sylvain VISSIERE-GUERINET
 */
@Event(target = Target.ALL)
public class FragmentDeletedEvent implements ISubscribable {

    private String fragmentName;

    public FragmentDeletedEvent() {
    }

    public FragmentDeletedEvent(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    public String getFragmentName() {
        return fragmentName;
    }

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
