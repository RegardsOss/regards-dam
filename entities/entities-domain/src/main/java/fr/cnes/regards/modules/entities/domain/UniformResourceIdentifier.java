/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;

/**
 * @author lmieulet
 *
 */
public class UniformResourceIdentifier {

    @NotNull
    private final URI uri_;

    public UniformResourceIdentifier(String pUrl) throws URISyntaxException {
        uri_ = new URI(pUrl);
    }

    public UniformResourceIdentifier(URI pUri) {
        uri_ = pUri;
    }

    /**
     * @return the URI
     */
    public URI getUri() {
        return uri_;
    }

    /**
     * @return URI scheme
     */
    public String getScheme() {
        return uri_.getScheme();
    }

    /**
     * @return URI host
     */
    public String getHost() {
        return uri_.getHost();
    }

    /**
     * @return URI path
     */
    public String getPath() {
        return uri_.getPath();
    }

    /**
     * @return URI port
     */
    public int getPort() {
        return uri_.getPort();
    }

    /**
     * @return URI fragment
     */
    public String getFragment() {
        return uri_.getFragment();
    }
}
