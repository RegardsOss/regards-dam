/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.signature;

import java.util.List;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * Fragment management API
 *
 * @author Marc Sordi
 *
 */
@RequestMapping("/models/fragments")
public interface IFragmentController {

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Resource<Fragment>>> getFragments();

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Resource<Fragment>> addFragment(@Valid @RequestBody Fragment pFragment) throws ModuleException;

    @RequestMapping(method = RequestMethod.GET, value = "/{pFragmentId}")
    ResponseEntity<Resource<Fragment>> getFragment(@PathVariable Long pFragmentId) throws ModuleException;

    @RequestMapping(method = RequestMethod.PUT, value = "/{pFragmentId}")
    ResponseEntity<Resource<Fragment>> updateFragment(@PathVariable Long pFragmentId,
            @Valid @RequestBody Fragment pFragment) throws ModuleException;

    @RequestMapping(method = RequestMethod.DELETE, value = "/{pFragmentId}")
    ResponseEntity<Void> deleteFragment(@PathVariable Long pFragmentId) throws ModuleException;
}
