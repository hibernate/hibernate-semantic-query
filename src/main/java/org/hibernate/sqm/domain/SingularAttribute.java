/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * Specialization of Attribute for non-collection (in the persistent sense) values
 *
 * @author Steve Ebersole
 */
public interface SingularAttribute extends Attribute, Bindable {
	/**
	 * Classifications of the singularity
	 */
	enum Classification {
		BASIC,
		EMBEDDED,
		ANY,
		ONE_TO_ONE,
		MANY_TO_ONE
	}

	/**
	 * Obtain the classification enum for the attribute.
	 *
	 * @return The classification
	 */
	Classification getAttributeTypeClassification();

	/**
	 * Obtain the attribute's type.
	 *
	 * @return The type
	 */
	Type getType();

	/**
	 * Is this attribute part of the identifier for the declaring type?
	 *
	 * @return {@code true} if it is part of the identifier; {@code false} otherwise
	 */
	boolean isId();

	/**
	 * Is this attribute the version for the declaring type?
	 *
	 * @return {@code true} if it is the version attribute; {@code false} otherwise
	 */
	boolean isVersion();
}
