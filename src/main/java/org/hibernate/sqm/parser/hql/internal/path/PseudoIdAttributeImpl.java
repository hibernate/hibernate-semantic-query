/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.IdentifierDescriptorMultipleAttribute;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * Used to model an entity's non-aggregated composite identifier as a SingularAttribute for binding
 *
 * @author Steve Ebersole
 */
class PseudoIdAttributeImpl implements SingularAttribute {
	private final EntityType entityType;

	public PseudoIdAttributeImpl(EntityType entityType) {
		this.entityType = entityType;
		assert entityType.getIdentifierDescriptor() instanceof IdentifierDescriptorMultipleAttribute;
	}

	@Override
	public Classification getAttributeTypeClassification() {
		return Classification.EMBEDDED;
	}

	@Override
	public Type getType() {
		return entityType.getIdentifierDescriptor().getIdType();
	}

	@Override
	public boolean isId() {
		return true;
	}

	@Override
	public boolean isVersion() {
		return false;
	}

	@Override
	public ManagedType getDeclaringType() {
		return entityType;
	}

	@Override
	public String getName() {
		return "id";
	}

	@Override
	public Type getBoundType() {
		return getType();
	}

	@Override
	public ManagedType asManagedType() {
		if ( getType() instanceof ManagedType ) {
			return (ManagedType) getType();
		}
		return null;
	}
}
