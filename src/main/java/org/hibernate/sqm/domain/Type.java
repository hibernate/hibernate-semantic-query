/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * A type-safe representation of a domain model type.  Generally, this type
 * describes just the Java model, not an "mapping" of that model to the
 * database which is more the purpose of a {@link Bindable}
 *
 * @author Steve Ebersole
 */
public interface Type {
	/**
	 * The name of the type.  For a Java type this would be the same as the type's Class name
	 *
	 * @return The type name
	 */
	String getTypeName();
}
