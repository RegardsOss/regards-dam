/**
 *
 */
package fr.cnes.regards.modules.feature.dto;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.util.Assert;

/**
 * @author kevin
 *
 */
public class FeatureSessionMetadata extends FeatureMetadata {

    public static final String MISSING_SESSION_OWNER = "Identifier of the session owner that submitted the feature is required";

    public static final String MISSING_SESSION = "Session is required";

    @NotBlank(message = MISSING_SESSION_OWNER)
    @Size(max = 128)
    private String sessionOwner;

    @NotBlank(message = MISSING_SESSION)
    @Size(max = 128)
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

    /**
    
     * Build feature metadata
     * @param sessionOwner Owner of the session
     * @param session session
     * @param storages storage metadata
     */
    public static FeatureSessionMetadata build(String sessionOwner, String session, PriorityLevel priority,
            StorageMetadata... storages) {
        return FeatureSessionMetadata.build(sessionOwner, session, priority, Arrays.asList(storages));
    }

    /**
     * Build feature metadata
     * @param sessionOwner Owner of the session
     * @param session session
     * @param storages storage metadata
     */
    public static FeatureSessionMetadata build(String sessionOwner, String session, PriorityLevel priority,
            List<StorageMetadata> storages) {
        Assert.hasLength(sessionOwner, MISSING_SESSION_OWNER);
        Assert.hasLength(session, MISSING_SESSION);
        Assert.notNull(storages, MISSING_STORAGE_METADATA);
        Assert.notNull(priority, MISSING_PRIORITY_LEVEL);
        FeatureSessionMetadata m = new FeatureSessionMetadata();
        m.setSessionOwner(sessionOwner);
        m.setSession(session);
        m.setStorages(storages);
        m.setPriority(priority);
        return m;
    }
}
