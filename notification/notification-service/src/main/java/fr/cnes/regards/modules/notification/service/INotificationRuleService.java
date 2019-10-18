/**
 *
 */
package fr.cnes.regards.modules.notification.service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;

/**
 * @author kevin
 *
 */
public interface INotificationRuleService {

    public boolean handleFeatures(Feature toHandle) throws NotAvailablePluginConfigurationException, ModuleException;
}
