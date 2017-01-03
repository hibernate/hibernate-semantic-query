/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.spi;

/**
 * Contract for identifiable types, which is taken from the JPA term for commonality between
 * entity and "mapped superclass" types.
 *
 * @author Steve Ebersole
 */
public interface IdentifiableType extends ManagedType, javax.persistence.metamodel.IdentifiableType {
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
	SingularAttributeBasic getVersionAttribute();
}
