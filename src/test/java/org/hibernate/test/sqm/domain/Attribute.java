/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.AttributeDescriptor;

/**
 * Represents an attribute in the domain model.
 *
 * @author Steve Ebersole
 */
public interface Attribute extends Bindable, AttributeDescriptor {
	@Override
	default ManagedType getLeftHandSide() {
		return getDeclaringType();
	}

	/**
	 * Obtain the type that declares the attribute.
	 *
	 * @return The attribute's declaring type.
	 */
	ManagedType getDeclaringType();
}
