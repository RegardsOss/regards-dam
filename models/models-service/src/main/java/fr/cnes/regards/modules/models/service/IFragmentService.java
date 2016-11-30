/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * Fragment service interface
 *
 * @author Marc Sordi
 *
 */
public interface IFragmentService {

    List<Fragment> getFragments();

    Fragment addFragment(Fragment pFragment) throws ModuleException;

    Fragment getFragment(Long pFragmentId) throws ModuleException;

    Fragment updateFragment(Long pFragmentId, Fragment pFragment) throws ModuleException;

    void deleteFragment(Long pFragmentId) throws ModuleException;

    void exportFragment(Long pFragmentId, OutputStream pOutputStream) throws ModuleException;

    void importFragment(InputStream pInputStream) throws ModuleException;
}
