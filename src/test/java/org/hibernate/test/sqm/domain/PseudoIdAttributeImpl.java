/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import java.util.Optional;

import org.hibernate.sqm.domain.EntityReference;

/**
 * Used to model an entity's non-aggregated composite identifier as a SingularAttribute for binding
 *
 * @author Steve Ebersole
 */
public class PseudoIdAttributeImpl implements SingularAttribute {
	private final IdentifiableType entityType;

	public PseudoIdAttributeImpl(IdentifiableType entityType) {
		this.entityType = entityType;
		assert entityType.getIdentifierDescriptor() instanceof IdentifierDescriptorMultipleAttribute;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.EMBEDDED;
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

	@Override
	public String getAttributeName() {
		return "<id>";
	}

	@Override
	public String asLoggableText() {
		return "ImplicitIdAttributeRef(" + getLeftHandSide().getTypeName() + ".<pk>)";
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.empty();
	}
}
