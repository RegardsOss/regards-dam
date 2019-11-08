package fr.cnes.regards.modules.crawler.service.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.modules.sessionmanager.domain.event.SessionMonitoringEvent;
import fr.cnes.regards.modules.sessionmanager.domain.event.SessionNotificationOperator;
import fr.cnes.regards.modules.sessionmanager.domain.event.SessionNotificationState;

/**
 * @author Sylvain VISSIERE-GUERINET
 */
@Component
public class SessionNotifier {

    /**
     * The name of the property gathering all metadata about this processing step
     */
    public static final String SESSION_NOTIF_STEP = "aip";

    public static final String PROPERTY_AIP_INDEXED = "indexed";
    
    public static final String PROPERTY_AIP_INDEXED_ERROR = "indexedError";

    @Autowired
    private IPublisher publisher;

    public void notifyIndexedSuccess(String sessionOwner, String session, long value) {
        notifyIncrementSession(sessionOwner, session, PROPERTY_AIP_INDEXED, SessionNotificationState.OK, value);
    }

    public void notifyIndexedError(String sessionOwner, String session, long value) {
        notifyIncrementSession(sessionOwner, session, PROPERTY_AIP_INDEXED_ERROR, SessionNotificationState.ERROR, value);
    }

    private void notifyIncrementSession(String sessionOwner, String session, String property, SessionNotificationState notifState, long value) {
        // Add one to the new state
        SessionMonitoringEvent event = SessionMonitoringEvent.build(
                sessionOwner,
                session,
                notifState,
                SESSION_NOTIF_STEP,
                SessionNotificationOperator.INC,
                property,
                value
        );
        publisher.publish(event);
    }

    private void notifyDecrementSession(String sessionOwner, String session, String property,
            SessionNotificationState notifState, long value) {
        // Add one to the new state
        SessionMonitoringEvent event = SessionMonitoringEvent.build(
                sessionOwner,
                session,
                notifState,
                SESSION_NOTIF_STEP,
                SessionNotificationOperator.DEC,
                property,
                value
        );
        publisher.publish(event);
    }

}
