/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.entities.domain.Data;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.plugins.annotations.PluginInterface;

/**
 * Plugin used to check if a {@link Data} from a {@link DataSet} is accessible, or not, for an {@link AccessGroup} or a
 * {@link User}
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@PluginInterface(
        description = "plugin used to check if a data from a dataset is accessible, or not, for an access group or a user")
public interface ICheckDataAccess extends IIdentifiable<Long> {

    boolean isAccessible(Data pData);

}
