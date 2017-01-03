/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import org.hibernate.test.sqm.type.spi.Type;

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

	@Override
	public String getTypeName() {
		return typeName;
	}
}
