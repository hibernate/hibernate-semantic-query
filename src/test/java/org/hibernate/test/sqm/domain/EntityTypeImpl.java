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
 * @author Steve Ebersole
 */
public class EntityTypeImpl extends AbstractIdentifiableType implements EntityType {
	public EntityTypeImpl(
			String typeName,
			IdentifiableType superType) {
		super( typeName, superType );
	}

	public EntityTypeImpl(
			Class javaType,
			IdentifiableType superType) {
		super( javaType, superType );
	}

	@Override
	public String getEntityName() {
		return getTypeName();
	}

	@Override
	public Type getBoundType() {
		return this;
	}

	@Override
	public ManagedType asManagedType() {
		return this;
	}

	@Override
	public String asLoggableText() {
		return "Entity(" + getEntityName() + ")";
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.of( this );
	}
}
