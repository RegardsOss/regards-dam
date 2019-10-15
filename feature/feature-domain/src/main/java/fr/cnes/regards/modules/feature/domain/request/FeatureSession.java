/**
 *
 */
package fr.cnes.regards.modules.feature.domain.request;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;

/**
 * Metadata associate to features requests
 * @author kevin
 *
 */
@Embeddable
public class FeatureSession {

    @NotEmpty
    @Column(length = 128, name = "session_owner", nullable = false)
    private String sessionOwner;

    @NotEmpty
    @Column(length = 128, name = "session_name", nullable = false)
    private String session;

    public String getSessionOwner() {
        return sessionOwner;
    }

    public void setSessionOwner(String sessionOwner) {
        this.sessionOwner = sessionOwner;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public static FeatureSession builder(String sessionOwner, String session) {
        FeatureSession newSession = new FeatureSession();
        newSession.setSession(session);
        newSession.setSessionOwner(sessionOwner);
        return newSession;
    }
}
