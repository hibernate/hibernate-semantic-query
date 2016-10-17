/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

/**
 * Contract for identifiable types, which is taken from the JPA term for commonality between
 * entity and "mapped superclass" types.
 *
 * @author Steve Ebersole
 */
public interface IdentifiableType extends ManagedType {
	/**
	 * Overridden to further qualify the super-type
	 * <p/>
	 * {@inheritDoc}
	 */
	@Override
	IdentifiableType getSuperType();

	/**
	 * Get the descriptor for the identifier declared on this type
	 *
	 * @return The identifier descriptor
	 */
	IdentifierDescriptor getIdentifierDescriptor();

	/**
	 * Get access to the attribute that defines optimistic locking (versioning) for this type.
	 *
	 * @return The version attribute
	 */
	SingularAttribute getVersionAttribute();
}
