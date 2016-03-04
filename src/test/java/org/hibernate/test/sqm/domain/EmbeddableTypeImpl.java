/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.EmbeddableType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class EmbeddableTypeImpl extends AbstractManagedType implements EmbeddableType {
	public EmbeddableTypeImpl(String typeName) {
		super( typeName );
	}

	public EmbeddableTypeImpl(Class javaType) {
		super( javaType );
	}

	@Override
	public ManagedType getSuperType() {
		// for now...
		return null;
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
