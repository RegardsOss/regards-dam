/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.parser;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.converter.PolygonToArray;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * This {@link IParser} implementation only handles the the "lat"/"lon"/"r" part of the OpenSearch request, and returns an {@link ICriterion} describing a circle intersection.<br>
 * @author Xavier-Alexandre Brochard
 */
@Component
public class GeometryParser implements IParser {

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.opensearch.service.parser.IParser#parse(java.util.Map)
     */
    @Override
    public ICriterion parse(Map<String, String> pParameters) throws OpenSearchParseException {
        Assert.notNull(pParameters.get("g"));

        try {
            WKTReader wkt = new WKTReader();
            Geometry geometry = wkt.read(pParameters.get("g"));

            if ("Polygon".equals(geometry.getGeometryType())) {
                Polygon polygon = (Polygon) geometry;
                Converter<Polygon, Double[][][]> converter = new PolygonToArray();
                Double[][][] coordinates = converter.convert(polygon);
                return ICriterion.intersectsPolygon(coordinates);
            } else {
                // Only Polygons are handled for now
                throw new OpenSearchParseException("The passed WKT string does not referecne");
            }

        } catch (ParseException e) {
            throw new OpenSearchParseException(e);
        }
    }

}
