/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

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
