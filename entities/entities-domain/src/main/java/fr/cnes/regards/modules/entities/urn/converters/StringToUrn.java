/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.urn.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.web.bind.annotation.ControllerAdvice;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Implement the type conversion logic for a String to a {@link UrnConverter}.<br>
 * This is automaticly used by Spring if need be.
 *
 * @author Xavier-Alexandre Brochard
 */
@ControllerAdvice
public class StringToUrn implements Converter<String, UniformResourceName> {

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public UniformResourceName convert(String pSource) {
        return UniformResourceName.fromString(pSource);
    }

}
