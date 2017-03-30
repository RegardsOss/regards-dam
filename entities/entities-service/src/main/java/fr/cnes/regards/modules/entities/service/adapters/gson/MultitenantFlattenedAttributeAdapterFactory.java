/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.gson.adapters.MultitenantPolymorphicTypeAdapterFactory;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactoryBean;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.BooleanAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.UrlAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.event.AttributeModelCreated;
import fr.cnes.regards.modules.models.domain.event.AttributeModelDeleted;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * Manage dynamic attribute (de)serialization
 *
 * @author Marc Sordi
 */
@SuppressWarnings("rawtypes")
@GsonTypeAdapterFactoryBean
public class MultitenantFlattenedAttributeAdapterFactory
        extends MultitenantPolymorphicTypeAdapterFactory<AbstractAttribute> {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultitenantFlattenedAttributeAdapterFactory.class);

    /**
     * Discriminator field
     */
    private static final String DISCRIMINATOR_FIELD_NAME = "name";

    /**
     * Discriminator field
     */
    private static final String VALUE_FIELD_NAME = "value";

    /**
     * Namespace separator
     */
    private static final String NS_SEPARATOR = ".";

    /**
     * Regexp escape character
     */
    private static final String REGEXP_ESCAPE = "\\";

    /**
     * {@link ITenantResolver}
     */
    private final ITenantResolver tenantResolver;

    /**
     * {@link AttributeModel} service
     */
    private final IAttributeModelService attributeModelService;

    /**
     * Subscriber listening to model change events to update attribute mapping dynamically
     */
    private final ISubscriber subscriber;

    public MultitenantFlattenedAttributeAdapterFactory(ITenantResolver pTenantResolver,
            IRuntimeTenantResolver pRuntimeTenantResolver, IAttributeModelService pAttributeModelService,
            ISubscriber pSubscriber) {
        super(pRuntimeTenantResolver, AbstractAttribute.class, DISCRIMINATOR_FIELD_NAME);
        tenantResolver = pTenantResolver;
        attributeModelService = pAttributeModelService;
        subscriber = pSubscriber;
        runtimeTenantResolver = pRuntimeTenantResolver;
    }

    public void registerSubtype(String pTenant, Class<?> pType, String pDiscriminatorFieldValue, String pNamespace) {
        if (pNamespace == null) {
            registerSubtype(pTenant, pType, pDiscriminatorFieldValue);
        } else {
            registerSubtype(pTenant, pType, pNamespace.concat(NS_SEPARATOR).concat(pDiscriminatorFieldValue));
        }
    }

    public void unregisterSubtype(String pTenant, Class<?> pType, String pDiscriminatorFieldValue, String pNamespace) {
        if (pNamespace == null) {
            unregisterSubtype(pTenant, pType, pDiscriminatorFieldValue);
        } else {
            unregisterSubtype(pTenant, pType, pNamespace.concat(NS_SEPARATOR).concat(pDiscriminatorFieldValue));
        }
    }

    @PostConstruct
    private void initSubtypes() {
        subscriber.subscribeTo(AttributeModelCreated.class, new RegisterHandler());
        subscriber.subscribeTo(AttributeModelDeleted.class, new UnregisterHandler());
        // Retrieve all tenants
        for (String tenant : tenantResolver.getAllTenants()) {
            // Set thread tenant to route database retrieval
            runtimeTenantResolver.forceTenant(tenant);
            // Register for tenant
            registerAttributes(tenant);
        }
    }

    public void refresh(String pTenant) {
        registerAttributes(pTenant);
    }

    /**
     * Dynamically register configured {@link AttributeModel} for a particular tenant
     *
     * @param pTenant tenant
     */
    protected void registerAttributes(String pTenant) {
        List<AttributeModel> atts = attributeModelService.getAttributes(null, null);
        if (atts != null) {
            for (AttributeModel att : atts) {
                // Define namespace if required
                String namespace = null;
                // Register namespace as an object wrapper
                if (!att.getFragment().isDefaultFragment()) {
                    namespace = att.getFragment().getName();
                    registerSubtype(pTenant, ObjectAttribute.class, namespace);
                }

                // Register attribute
                registerSubtype(pTenant, getClassByType(att.getType()), att.getName(), namespace);
            }
        }
    }

    /**
     * @param pAttributeType {@link AttributeType}
     * @return corresponding {@link Serializable} class
     */
    protected Class<?> getClassByType(AttributeType pAttributeType) { // NOSONAR
        // Retrieve matching attribute class
        Class<?> matchingClass;
        switch (pAttributeType) {
            case BOOLEAN:
                matchingClass = BooleanAttribute.class;
                break;
            case DATE_ARRAY:
                matchingClass = DateArrayAttribute.class;
                break;
            case DATE_INTERVAL:
                matchingClass = DateIntervalAttribute.class;
                break;
            case DATE_ISO8601:
                matchingClass = DateAttribute.class;
                break;
            case DOUBLE:
                matchingClass = DoubleAttribute.class;
                break;
            case DOUBLE_ARRAY:
                matchingClass = DoubleArrayAttribute.class;
                break;
            case DOUBLE_INTERVAL:
                matchingClass = DoubleIntervalAttribute.class;
                break;
            case INTEGER:
                matchingClass = IntegerAttribute.class;
                break;
            case INTEGER_ARRAY:
                matchingClass = IntegerArrayAttribute.class;
                break;
            case INTEGER_INTERVAL:
                matchingClass = IntegerIntervalAttribute.class;
                break;
            case STRING:
                matchingClass = StringAttribute.class;
                break;
            case STRING_ARRAY:
                matchingClass = StringArrayAttribute.class;
                break;
            case URL:
                matchingClass = UrlAttribute.class;
                break;
            default:
                String errorMessage = String.format("Unexpected attribute type \"%s\".", pAttributeType);
                LOGGER.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
        }
        return matchingClass;
    }

    @Override
    protected JsonElement getOnReadDiscriminator(JsonElement pJsonElement) {
        JsonElement discriminator = null;
        if (pJsonElement.isJsonObject()) {
            JsonObject o = pJsonElement.getAsJsonObject();
            if (o.size() != 1) {
                String errorMessage = String.format("Only single key/value pair is expected in \"%s\"", pJsonElement);
                LOGGER.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
                return new JsonPrimitive(entry.getKey());
            }
        }
        return discriminator;
    }

    @Override
    protected JsonElement beforeRead(JsonElement pJsonElement, String pDiscriminator, Class<?> pSubType) {
        JsonElement restored = restore(pJsonElement, pSubType);
        if (pSubType == ObjectAttribute.class) {
            addNamespaceToChildren(restored, pDiscriminator);
        }
        removeParentNamespace(restored);
        return restored;
    }

    @Override
    protected JsonElement beforeWrite(JsonElement pJsonElement, Class<?> pSubType) {
        return flatten(pJsonElement, pSubType);
    }

    /**
     * Flatten a {@link JsonElement} carrying key and value in separated fields into a single field whose key is the
     * value of the key field and value the value of the value field
     *
     * @param pJsonElement {@link JsonElement} to flatten
     * @param pSubType sub type
     * @return flattened {@link JsonElement}
     */
    protected JsonElement flatten(JsonElement pJsonElement, Class<?> pSubType) {
        LOGGER.debug(String.format("Flattening %s", pJsonElement));

        if (!pJsonElement.isJsonObject()) {
            String format = "JSON element must be an object containing 2 members whose names are \"%s\" and \"%s\"";
            String errorMessage = String.format(format, DISCRIMINATOR_FIELD_NAME, VALUE_FIELD_NAME);
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        JsonObject current = pJsonElement.getAsJsonObject();

        // Init flattened element
        JsonObject flattened = new JsonObject();
        // Get key : must be a string
        JsonElement key = current.get(DISCRIMINATOR_FIELD_NAME);
        // Get value
        JsonElement val = current.get(VALUE_FIELD_NAME);

        if (pSubType == ObjectAttribute.class) {
            // Flattening array elements
            JsonObject flattenedObject = new JsonObject();
            Iterator<JsonElement> nestedIter = val.getAsJsonArray().iterator();
            while (nestedIter.hasNext()) {
                JsonObject nested = nestedIter.next().getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : nested.entrySet()) {
                    flattenedObject.add(e.getKey(), e.getValue());
                }
            }
            flattened.add(key.getAsString(), flattenedObject);
        } else {
            flattened.add(key.getAsString(), val);
        }

        LOGGER.debug(String.format("Flattened object : \"%s\" -> \"%s\"", pJsonElement, flattened));

        return flattened;
    }

    /**
     * Restore {@link JsonElement} object structure (inverse flattening)
     *
     * @param pJsonElement {@link JsonElement} to restore
     * @param pSubType sub type
     * @return restored {@link JsonElement}
     */
    protected JsonElement restore(JsonElement pJsonElement, Class<?> pSubType) {
        LOGGER.debug(String.format("Restoring %s", pJsonElement));

        if (!pJsonElement.isJsonObject()) {
            String errorMessage = "JSON element must be an object.";
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        JsonObject current = pJsonElement.getAsJsonObject();

        // Init restored element
        JsonObject restored = new JsonObject();
        // Restore members
        for (Map.Entry<String, JsonElement> e : current.entrySet()) {
            restored.addProperty(DISCRIMINATOR_FIELD_NAME, e.getKey());
            JsonElement val = e.getValue();
            if (pSubType == ObjectAttribute.class) {
                // Restoring array but not element structure
                JsonArray restoredArray = new JsonArray();
                for (Map.Entry<String, JsonElement> nestedEntry : val.getAsJsonObject().entrySet()) {
                    JsonObject nestedObject = new JsonObject();
                    nestedObject.add(nestedEntry.getKey(), nestedEntry.getValue());
                    restoredArray.add(nestedObject);
                }
                restored.add(VALUE_FIELD_NAME, restoredArray);
            } else {
                restored.add(VALUE_FIELD_NAME, val);
            }
        }

        LOGGER.debug(String.format("Restored object : \"%s\" -> \"%s\"", pJsonElement, restored));

        return restored;
    }

    /**
     * Add namespace to {@link JsonElement} children of {@link ObjectAttribute}
     *
     * @param pJsonElement {@link JsonElement}
     * @param pDiscriminator discriminator value
     */
    protected void addNamespaceToChildren(JsonElement pJsonElement, String pDiscriminator) {

        if (pJsonElement.isJsonObject()) {
            final JsonElement children = pJsonElement.getAsJsonObject().get(VALUE_FIELD_NAME);

            if (children == null) {
                throw missingFieldException(pJsonElement, VALUE_FIELD_NAME);
            }

            if (children.isJsonArray()) {
                final Iterator<JsonElement> childrenIter = children.getAsJsonArray().iterator();
                while (childrenIter.hasNext()) {
                    addNamespaceToChild(childrenIter.next(), pDiscriminator);
                }
            } else {
                throw objectRequiredException(pJsonElement);
            }
        } else {
            throw objectRequiredException(pJsonElement);
        }
    }

    /**
     * Add namespace to {@link JsonElement} child keys
     *
     * @param pJsonElement {@link JsonElement}
     * @param pDiscriminator discriminator value
     */
    protected void addNamespaceToChild(JsonElement pJsonElement, String pDiscriminator) {

        if (pJsonElement.isJsonObject()) {

            // Backup for logging
            String logOriginal = pJsonElement.toString();
            JsonObject o = pJsonElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
                // Add new key mapping
                o.add(pDiscriminator.concat(NS_SEPARATOR).concat(entry.getKey()), entry.getValue());
                // Remove old key mapping
                o.remove(entry.getKey());
            }

            LOGGER.debug(String.format("Namespace added : \"%s\" -> \"%s\"", logOriginal, pJsonElement.toString()));
        } else {
            throw objectRequiredException(pJsonElement);
        }
    }

    /**
     * Remove namespace from {@link JsonElement}
     *
     * @param pJsonElement target {@link JsonElement}
     */
    protected void removeParentNamespace(JsonElement pJsonElement) {

        if (pJsonElement.isJsonObject()) {

            // Backup for logging
            String logOriginal = pJsonElement.toString();

            final JsonObject o = pJsonElement.getAsJsonObject();
            final JsonElement nsElement = o.get(DISCRIMINATOR_FIELD_NAME);
            if (nsElement == null) {
                throw missingFieldException(pJsonElement, DISCRIMINATOR_FIELD_NAME);
            }

            // Compute and inject name without its namespace
            String nsName = nsElement.getAsString();
            String[] splitNsName = nsName.split(REGEXP_ESCAPE + NS_SEPARATOR);
            o.add(DISCRIMINATOR_FIELD_NAME, new JsonPrimitive(splitNsName[splitNsName.length - 1]));

            if (LOGGER.isDebugEnabled()) {
                if (splitNsName.length > 1) {
                    LOGGER.debug(String.format("Namespace removed : \"%s\" -> \"%s\"", logOriginal,
                                               pJsonElement.toString()));
                } else {
                    LOGGER.debug(String.format("No namespace to remove : \"%s\" -> \"%s\"", logOriginal,
                                               pJsonElement.toString()));
                }
            }
        } else {
            throw objectRequiredException(pJsonElement);
        }
    }

    private IllegalArgumentException objectRequiredException(JsonElement pJsonElement) {
        String errorMessage = String.format("Unexpected JSON element %s. Object required.", pJsonElement.toString());
        LOGGER.error(errorMessage);
        return new IllegalArgumentException(errorMessage);
    }

    private IllegalArgumentException missingFieldException(JsonElement pJsonElement, String pFieldName) {
        String errorMessage = String.format("JSON element %s must contains a \"%s\" field", pJsonElement.toString(),
                                            pFieldName);
        LOGGER.error(errorMessage);
        return new IllegalArgumentException(errorMessage);
    }

    /**
     * Handle {@link AttributeModel} creation
     *
     * @author Marc Sordi
     */
    private class RegisterHandler implements IHandler<AttributeModelCreated> {

        @Override
        public void handle(TenantWrapper<AttributeModelCreated> pWrapper) {
            AttributeModelCreated amc = pWrapper.getContent();
            registerSubtype(pWrapper.getTenant(), getClassByType(amc.getAttributeType()), amc.getAttributeName(),
                            amc.getFragmentName());
        }
    }

    /**
     * Handle {@link AttributeModel} deletion
     *
     * @author Marc Sordi
     */
    private class UnregisterHandler implements IHandler<AttributeModelDeleted> {

        @Override
        public void handle(TenantWrapper<AttributeModelDeleted> pWrapper) {
            AttributeModelDeleted amd = pWrapper.getContent();
            unregisterSubtype(pWrapper.getTenant(), getClassByType(amd.getAttributeType()), amd.getAttributeName(),
                              amd.getFragmentName());
        }
    }
}
