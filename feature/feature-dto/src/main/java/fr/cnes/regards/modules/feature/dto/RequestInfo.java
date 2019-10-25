/**
 *
 */
package fr.cnes.regards.modules.feature.dto;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;

/**
 * Information dto about Creation/Update feature
 *
 * Will contain for creation:
 * All request id generated during {@link FeatureCreationRequest} creation,
 * all granted request id and all errors by request id
 *
 * Will contain for Update
 * All urn generated during {@link FeatureUpdateRequest} update,
 * all granted urn and all errors by urn
 * @author kevin
 *
 */
public class RequestInfo<T> {

    private Map<String, T> idByFeatureId;

    private Multimap<T, String> errorById;

    private Set<T> grantedId;

    public Map<String, T> getIdByFeatureId() {
        return idByFeatureId;
    }

    public void setIdByFeatureId(Map<String, T> idByFeatureId) {
        this.idByFeatureId = idByFeatureId;
    }

    public Multimap<T, String> getErrorById() {
        return errorById;
    }

    public void setErrorById(Multimap<T, String> errorById) {
        this.errorById = errorById;
    }

    public Set<T> getGrantedId() {
        return grantedId;
    }

    public void setGrantedId(Set<T> grantedId) {
        this.grantedId = grantedId;
    }

}
