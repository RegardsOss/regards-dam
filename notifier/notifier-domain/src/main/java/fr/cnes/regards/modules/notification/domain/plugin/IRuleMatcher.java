/**
 *
 */
package fr.cnes.regards.modules.notification.domain.plugin;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.feature.dto.Feature;

/**
 * @author kevin
 *
 */
@FunctionalInterface
@PluginInterface(description = "Feature rule matcher")
public interface IRuleMatcher {

    /**
     * Verify if a {@link Feature} match with a rule
     * @param feature {@link Feature} to verify if it maches
     * @return
     */
    boolean match(Feature feature);
}
