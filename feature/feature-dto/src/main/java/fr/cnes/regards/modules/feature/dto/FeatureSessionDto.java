/**
 *
 */
package fr.cnes.regards.modules.feature.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author kevin
 *
 */
public class FeatureSessionDto {

    @NotEmpty
    @Size(max = 128)
    private String sessionOwner;

    @NotEmpty
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
     * @param string
     * @param string2
     * @return
     */
    public static FeatureSessionDto builder(String owner, String session) {
        FeatureSessionDto sessionDto = new FeatureSessionDto();
        sessionDto.setSession(session);
        sessionDto.setSessionOwner(owner);

        return sessionDto;
    }

}
