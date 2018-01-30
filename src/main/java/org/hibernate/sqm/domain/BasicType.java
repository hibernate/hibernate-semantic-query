/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * A basic domain model type.  Even though the type system here is tye safe, basic
 * types are defined in terms of a Java type.
 *
 * @author Steve Ebersole
 */
public interface BasicType extends Navigable {
	/**
	 * Return the represented Java type.
	 *
	 * @return Java type
	 */
	Class getJavaType();
}
