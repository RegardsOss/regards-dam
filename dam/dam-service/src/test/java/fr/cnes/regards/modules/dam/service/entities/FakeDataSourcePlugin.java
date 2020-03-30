package fr.cnes.regards.modules.dam.service.entities;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DataSourceException;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.dam.domain.entities.feature.DataObjectFeature;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * Data source plugin
 *
 * @author Christophe Mertz
 */
@Plugin(id = "FakeDataSourcePlugin", version = "2.0-SNAPSHOT", description = "FakeDataSourcePlugin",
        author = "REGARDS Team", contact = "regards@c-s.fr", license = "GPLv3", owner = "CSSI",
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
    public Page<DataObjectFeature> findAll(String tenant, Pageable pageable, OffsetDateTime date)
            throws DataSourceException {
        List<DataObjectFeature> content = new ArrayList<>();
        return new PageImpl<>(content);
    }
}
