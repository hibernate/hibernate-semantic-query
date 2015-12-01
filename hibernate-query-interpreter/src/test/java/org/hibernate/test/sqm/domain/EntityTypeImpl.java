/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.IdentifiableType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;

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
	public String getName() {
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
}
