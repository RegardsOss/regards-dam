/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactory;
import fr.cnes.regards.modules.entities.domain.Document;

/**
 *
 * {@link Document} adapter factory
 *
 * @author Marc Sordi
 *
 */
@GsonTypeAdapterFactory
public class DocumentTypeAdapterFactory extends AbstractEntityTypeAdapterFactory<Document> {

    public DocumentTypeAdapterFactory() {
        super(Document.class);
    }
}
