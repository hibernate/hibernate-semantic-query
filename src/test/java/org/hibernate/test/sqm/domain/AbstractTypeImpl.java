/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

/**
 * Abstract support for all Type implementations.  Mainly adds "type name" handling.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractTypeImpl implements Type {
	private final String typeName;

	public AbstractTypeImpl(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}
}
