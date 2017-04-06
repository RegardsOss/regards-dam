/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.entities.domain.DataFile;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Plugin used to check if a {@link DataFile} from a {@link Dataset} is accessible, or not, for an {@link AccessGroup} or a
 * {@link User}
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@PluginInterface(
        description = "plugin used to check if a data from a dataset is accessible, or not, for an access group or a user")
public interface ICheckDataAccess extends IIdentifiable<Long> {

    boolean isAccessible(DataFile pData);

}
