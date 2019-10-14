/**
 *
 */
package fr.cnes.regards.modules.feature.domain.request;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Metadata associate to features requests
 * @author kevin
 *
 */
@Embeddable
public class FeatureSession {

    // FIXME qu'elles contrainte?
    @Column(length = 128, name = "session_owner")
    private String sessionOwner;

    // FIXME qu'elles contrainte?
    @Column(length = 128, name = "session_name")
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
}
