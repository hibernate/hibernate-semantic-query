/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * Represents an attribute in the domain model.
 *
 * @author Steve Ebersole
 */
public interface Attribute extends Bindable {
	/**
	 * Obtain the type that declares the attribute.
	 *
	 * @return The attribute's declaring type.
	 */
	ManagedType getDeclaringType();

	/**
	 * Obtain the attribute's name
	 *
	 * @return The name
	 */
	String getName();
}
