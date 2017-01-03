/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import org.hibernate.test.sqm.type.spi.BasicType;

/**
 * @author Steve Ebersole
 */
public class BasicTypeImpl<T> extends AbstractTypeImpl implements BasicType<T> {
	private final Class<T> javaType;

	public BasicTypeImpl(Class<T> javaType) {
		super( javaType.getName() );
		this.javaType = javaType;
	}

	@Override
	public Class<T> getJavaType() {
		return javaType;
	}

	@Override
	public String asLoggableText() {
		return "BasicType(" + javaType + ")";
	}
}
