/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * @author Steve Ebersole
 */
public interface SingularAttribute extends Attribute, Bindable {
	enum Classification {
		BASIC,
		EMBEDDED,
		ANY,
		ONE_TO_ONE,
		MANY_TO_ONE
	}

	Classification getAttributeTypeClassification();

	Type getType();

	boolean isId();

	boolean isVersion();
}
