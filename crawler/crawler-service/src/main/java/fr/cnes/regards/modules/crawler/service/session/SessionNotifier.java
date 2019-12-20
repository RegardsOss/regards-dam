package fr.cnes.regards.modules.crawler.service.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
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
    public static final String SESSION_NOTIF_STEP = "catalog";

    public static final String PROPERTY_AIP_INDEXED = "indexed";

    public static final String PROPERTY_AIP_INDEXED_ERROR = "indexedError";

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionNotifier.class);

    @Autowired
    private IPublisher publisher;

    public void notifyIndexedSuccess(String sessionOwner, String session, long value) {
        if (!Strings.isNullOrEmpty(sessionOwner) && !Strings.isNullOrEmpty(session)) {
            notifyIncrementSession(sessionOwner, session, PROPERTY_AIP_INDEXED, SessionNotificationState.OK, value);
        } else {
            LOGGER.debug(String.format(
                    "Session has not been notified of successful indexation of %s features because either sessionOwner(%s) or session(%s) is null or empty",
                    value,
                    sessionOwner,
                    session));
        }
    }

    public void notifyIndexedError(String sessionOwner, String session, long value) {
        if (!Strings.isNullOrEmpty(sessionOwner) && !Strings.isNullOrEmpty(session)) {
            notifyIncrementSession(sessionOwner,
                                   session,
                                   PROPERTY_AIP_INDEXED_ERROR,
                                   SessionNotificationState.ERROR,
                                   value);
        } else {
            LOGGER.debug(String.format(
                    "Session has not been notified of unsuccessful indexation of %s features because either sessionOwner(%s) or session(%s) is null or empty",
                    value,
                    sessionOwner,
                    session));
        }
    }

    private void notifyIncrementSession(String sessionOwner, String session, String property,
            SessionNotificationState notifState, long value) {
        SessionMonitoringEvent event = SessionMonitoringEvent.build(sessionOwner,
                                                                    session,
                                                                    notifState,
                                                                    SESSION_NOTIF_STEP,
                                                                    SessionNotificationOperator.INC,
                                                                    property,
                                                                    value);
        publisher.publish(event);
    }

    private void notifyDecrementSession(String sessionOwner, String session, String property,
            SessionNotificationState notifState, long value) {
        SessionMonitoringEvent event = SessionMonitoringEvent.build(sessionOwner,
                                                                    session,
                                                                    notifState,
                                                                    SESSION_NOTIF_STEP,
                                                                    SessionNotificationOperator.DEC,
                                                                    property,
                                                                    value);
        publisher.publish(event);
    }

    public void notifyIndexDeletion() {
        SessionMonitoringEvent event = SessionMonitoringEvent.buildGlobal(SessionNotificationState.OK,
                                                                          SESSION_NOTIF_STEP,
                                                                          SessionNotificationOperator.REPLACE,
                                                                          PROPERTY_AIP_INDEXED,
                                                                          0L);
        publisher.publish(event);

        event = SessionMonitoringEvent.buildGlobal(SessionNotificationState.OK,
                                                   SESSION_NOTIF_STEP,
                                                   SessionNotificationOperator.REPLACE,
                                                   PROPERTY_AIP_INDEXED_ERROR,
                                                   0L);
        publisher.publish(event);
    }
}
