package fr.cnes.regards.modules.entities.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.domain.plugins.DataSourceException;
import fr.cnes.regards.modules.datasources.domain.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Data source plugin
 *
 * @author Christophe Mertz
 */
@Plugin(id = "FakeDataSourcePlugin", version = "2.0-SNAPSHOT", description = "FakeDataSourcePlugin",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class FakeDataSourcePlugin implements IDataSourcePlugin {
    
    public static final String MODEL = "model";

    @PluginParameter(label = MODEL, name = MODEL)
    private Model dataModel;


    @Override
    public int getRefreshRate() {
        return 0;
    }

    @Override
    public String getModelName() {
        return dataModel.getName();
    }

    @Override
    public Page<DataObject> findAll(String tenant, Pageable pageable, OffsetDateTime date) throws DataSourceException {
        List<DataObject> content = new ArrayList<>();
        return new PageImpl<>(content);
    }
}
