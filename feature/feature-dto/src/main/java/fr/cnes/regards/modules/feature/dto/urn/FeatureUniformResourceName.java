/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.feature.dto.urn;

import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.persistence.Convert;

import fr.cnes.regards.framework.oais.urn.AbstractUniformResourceName;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.converters.UrnConverter;
import fr.cnes.regards.framework.oais.urn.validator.RegardsOaisUrn;

/**
 * allow us to create URN with the following format:
 * URN:FeatureIdentifier:entityType:tenant:UUID(entityId):Vversion[,order][:REVrevision]
 *
 * <br/>
 * Example:
 * <ul>
 * <li>URN:FEATURE:Collection:CDPP::1</li>
 * <li>URN:FEATURE:Collection:CDPP::1,5:REV2</li>
 * </ul>
 *
 * @author Kevin Marchois
 */
@RegardsOaisUrn
@Convert(converter = UrnConverter.class)
public class FeatureUniformResourceName extends AbstractUniformResourceName<FeatureIdentifier> {

	public static final int MAX_SIZE = 128;

	/**
	 * URN pattern
	 */
	public static final String URN_PATTERN = "URN:[^:]+:[^:]+:[^:]+:[^:]+:V\\d{1,3}(,\\d+)?(:REV.+)?";

	/**
	 * Version prefix
	 */
	private static final String VERSION_PREFIX = "V";

	/**
	 * Section delimiter
	 */
	private static final String DELIMITER = ":";

	/**
	 * Revision prefix
	 */
	private static final String REVISION_PREFIX = "REV";

	private static final String BASE_URN_ZERO = "00000000-0000-0000-0000";

	/**
	 * Compiled pattern
	 */
	private static final Pattern PATTERN = Pattern.compile(URN_PATTERN);

	/**
	 * Constructor setting the given parameters as attributes
	 */
	public FeatureUniformResourceName(FeatureIdentifier oaisIdentifier, EntityType entityType, String tenant,
			UUID entityId, int version) {
		super(oaisIdentifier, entityType, tenant, entityId, version);
	}

	/**
	 * Constructor setting the given parameters as attributes
	 */
	public FeatureUniformResourceName(FeatureIdentifier oaisIdentifier, EntityType entityType, String tenant,
			UUID entityId, int version, Long order, String revision) {
		super(oaisIdentifier, entityType, tenant, entityId, version, order, revision);
	}

	/**
	 * Constructor setting the given parameters as attributes
	 */
	public FeatureUniformResourceName(FeatureIdentifier oaisIdentifier, EntityType entityType, String tenant,
			UUID entityId, int version, long order) {
		super(oaisIdentifier, entityType, tenant, entityId, version, order);
	}

	/**
	 * Constructor setting the given parameters as attributes
	 */
	public FeatureUniformResourceName(FeatureIdentifier oaisIdentifier, EntityType entityType, String tenant,
			UUID entityId, int version, String revision) {
		super(oaisIdentifier, entityType, tenant, entityId, version, revision);
	}

	public FeatureUniformResourceName() {
		// for testing purpose
	}

	/**
	 * take this kind of String
	 * URN:OAISIdentifier:entityType:tenant:UUID(entityId):version[,order][:REVrevision]
	 * and return a new instance of {@link FeatureUniformResourceName}
	 *
	 * @param urn String respecting the following regex
	 *            URN:.+:.+:.+:.+:\\d{1,3}(,\\d+)?(:REV.+)?
	 * @return a new instance of {@link FeatureUniformResourceName}
	 * @throws IllegalArgumentException if the given string does not respect the urn
	 *                                  pattern
	 */
	public static FeatureUniformResourceName fromString(String urn) {
		final Pattern pattern = Pattern.compile(URN_PATTERN);
		if (!pattern.matcher(urn).matches()) {
			throw new IllegalArgumentException();
		}
		final String[] stringFragment = urn.split(DELIMITER);
		final FeatureIdentifier oaisIdentifier = FeatureIdentifier.valueOf(stringFragment[1]);
		final EntityType entityType = EntityType.valueOf(stringFragment[2]);
		final String tenant = stringFragment[3];
		final UUID entityId = UUID.fromString(stringFragment[4]);
		final String[] versionWithOrder = stringFragment[5].split(",");
		if (versionWithOrder.length == 2) {
			// Order is precised
			if (stringFragment.length == 7) {
				// Revision is precised
				final String revisionString = stringFragment[6];
				// so we have all fields
				return new FeatureUniformResourceName(oaisIdentifier, entityType, tenant, entityId,
						Integer.parseInt(versionWithOrder[0].substring(VERSION_PREFIX.length())),
						Long.parseLong(versionWithOrder[1]), revisionString.substring(REVISION_PREFIX.length()));
			} else {
				// Revision is missing so we have all except Revision
				return new FeatureUniformResourceName(oaisIdentifier, entityType, tenant, entityId,
						Integer.parseInt(versionWithOrder[0].substring(VERSION_PREFIX.length())),
						Long.parseLong(versionWithOrder[1]));
			}
		} else {
			// we don't have an order specified
			if (stringFragment.length == 7) {
				// Revision is precised
				final String revisionString = stringFragment[6];
				// so we have all fields exception Order
				return new FeatureUniformResourceName(oaisIdentifier, entityType, tenant, entityId,
						Integer.parseInt(versionWithOrder[0].substring(VERSION_PREFIX.length())),
						revisionString.substring(REVISION_PREFIX.length()));
			} else {
				// Revision is missing so we have all except Revision and Order
				return new FeatureUniformResourceName(oaisIdentifier, entityType, tenant, entityId,
						Integer.parseInt(versionWithOrder[0].substring(VERSION_PREFIX.length())));
			}
		}
	}

	/**
	 * Build a pseudo random UUID starting with 00000000-0000-0000-0000
	 */
	public static FeatureUniformResourceName pseudoRandomUrn(FeatureIdentifier oaisIdentifier, EntityType entityType,
			String tenant, int version) {
		return new FeatureUniformResourceName(oaisIdentifier, entityType, tenant,
				UUID.fromString("0-0-0-0-" + (int) (Math.random() * Integer.MAX_VALUE)), version);
	}

	public static FeatureUniformResourceName clone(FeatureUniformResourceName template, Long order) {
		return new FeatureUniformResourceName(template.getIdentifier(), template.getEntityType(), template.getTenant(),
				template.getEntityId(), template.getVersion(), order);
	}

	/**
	 * By default UUID.randomUUID() must not be used. It is generating a true random
	 * UUID which makes it undetectable. To avoid this, pseudo random UUID is used
	 * with following format : 00000000-0000-0000-0000-&lt;random-int>
	 */
	public boolean isRandomEntityId() {
		return super.getEntityId().toString().startsWith(BASE_URN_ZERO);
	}

	/**
	 * @return whether the given string is a urn or not
	 */
	public static boolean isValidUrn(String urn) {
		return PATTERN.matcher(urn).matches();
	}

	@Override
	public String toString() {
		final StringJoiner urnBuilder = new StringJoiner(":", "URN:", "");
		urnBuilder.add(super.getIdentifier().toString());
		urnBuilder.add(super.getEntityType().toString());
		urnBuilder.add(super.getTenant());
		urnBuilder.add(super.getEntityId().toString());
		String orderString = "";
		if (super.getOrder() != null) {
			orderString = "," + super.getOrder();
		}
		// order is not added with the joiner because it is "version,order" and not
		// "version:order"
		urnBuilder.add(VERSION_PREFIX + super.getVersion() + orderString);
		if (super.getRevision() != null) {
			urnBuilder.add(REVISION_PREFIX + super.getRevision());
		}
		return urnBuilder.toString();
	}

	/**
	 * @return the oais identifier
	 */
	public FeatureIdentifier getOaisIdentifier() {
		return super.getIdentifier();
	}

	/**
	 * Set the oais identifier
	 */
	public void setOaisIdentifier(FeatureIdentifier oaisIdentifier) {
		super.setIdentifier(oaisIdentifier);
	}

	/**
	 * @return the entity type
	 */
	@Override
	public EntityType getEntityType() {
		return super.getEntityType();
	}

	/**
	 * Set the entity type
	 */
	@Override
	public void setEntityType(EntityType entityType) {
		super.setEntityType(entityType);
	}

	/**
	 * @return the tenant
	 */
	@Override
	public String getTenant() {
		return super.getTenant();
	}

	/**
	 * Set the tenant
	 */
	@Override
	public void setTenant(String tenant) {
		super.setTenant(tenant);
	}

	/**
	 * @return the entity id
	 */
	@Override
	public UUID getEntityId() {
		return super.getEntityId();
	}

	/**
	 * Set the entity id
	 */
	@Override
	public void setEntityId(UUID entityId) {
		super.setEntityId(entityId);
	}

	/**
	 * @return the version
	 */
	@Override
	public int getVersion() {
		return super.getVersion();
	}

	/**
	 * Set the version
	 */
	@Override
	public void setVersion(int version) {
		super.setVersion(version);
	}

	/**
	 * @return the order
	 */
	@Override
	public Long getOrder() {
		return super.getOrder();
	}

	/**
	 * Set the order
	 */
	@Override
	public void setOrder(Long order) {
		super.setOrder(order);
	}

	/**
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return super.getRevision();
	}

	/**
	 * Set the revision
	 */
	@Override
	public void setRevision(String revision) {
		super.setRevision(revision);
	}
}
