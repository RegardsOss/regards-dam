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
    private final URI uri;

    public UniformResourceIdentifier(String pUrl) throws URISyntaxException {
        uri = new URI(pUrl);
    }

    public UniformResourceIdentifier(URI pUri) {
        uri = pUri;
    }

    /**
     * @return the URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @return URI scheme
     */
    public String getScheme() {
        return uri.getScheme();
    }

    /**
     * @return URI host
     */
    public String getHost() {
        return uri.getHost();
    }

    /**
     * @return URI path
     */
    public String getPath() {
        return uri.getPath();
    }

    /**
     * @return URI port
     */
    public int getPort() {
        return uri.getPort();
    }

    /**
     * @return URI fragment
     */
    public String getFragment() {
        return uri.getFragment();
    }
}
