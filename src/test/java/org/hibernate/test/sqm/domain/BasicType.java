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
public interface BasicType<T> extends Type, org.hibernate.sqm.domain.BasicType {
	/**
	 * Return the represented Java type.
	 *
	 * @return Java type
	 */
	Class<T> getJavaType();
}
