/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import org.hibernate.test.sqm.type.spi.ManagedType;
import org.hibernate.test.sqm.type.spi.SingularAttribute;
import org.hibernate.test.sqm.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSingularAttribute implements SingularAttribute {
	private final ManagedType declaringType;
	private final String name;
	private final Type type;

	public AbstractSingularAttribute(
			ManagedType declaringType,
			String name,
			Type type) {
		this.declaringType = declaringType;
		this.name = name;
		this.type = type;
	}

	@Override
	public ManagedType getDeclaringType() {
		return declaringType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "SingularAttribute(" + declaringType.getTypeName() + "." + name + " : " + type.getTypeName() + ")";
	}
}
