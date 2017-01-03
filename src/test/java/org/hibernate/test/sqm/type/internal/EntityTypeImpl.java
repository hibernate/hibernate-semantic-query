/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import java.util.Optional;

import org.hibernate.sqm.domain.EntityReference;

import org.hibernate.test.sqm.type.spi.EntityType;
import org.hibernate.test.sqm.type.spi.IdentifiableType;

/**
 * @author Steve Ebersole
 */
public class EntityTypeImpl extends AbstractIdentifiableType implements EntityType  {
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
	public String asLoggableText() {
		return "Entity(" + getEntityName() + ")";
	}
}
