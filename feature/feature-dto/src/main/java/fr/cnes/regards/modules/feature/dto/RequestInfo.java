/**
 *
 */
package fr.cnes.regards.modules.feature.dto;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;

/**
 *
 * @author kevin
 *
 */
public class RequestInfo {

    private Map<String, String> requestIdByFeatureId;

    private Multimap<String, String> errorbyRequestId;

    private Set<String> grantedRequestId;

    public Map<String, String> getRequestIdByFeatureId() {
        return requestIdByFeatureId;
    }

    public void setRequestIdByFeatureId(Map<String, String> requestIdByFeatureId) {
        this.requestIdByFeatureId = requestIdByFeatureId;
    }

    public Multimap<String, String> getErrorbyRequestId() {
        return errorbyRequestId;
    }

    public void setErrorbyRequestId(Multimap<String, String> errorbyRequestId) {
        this.errorbyRequestId = errorbyRequestId;
    }

    public Set<String> getGrantedRequestId() {
        return grantedRequestId;
    }

    public void setGrantedRequestId(Set<String> grantedRequestId) {
        this.grantedRequestId = grantedRequestId;
    }

}
