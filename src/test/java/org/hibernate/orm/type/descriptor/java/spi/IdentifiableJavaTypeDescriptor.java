/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.spi;

/**
 * Contract for identifiable types, which is taken from the JPA term for commonality between
 * entity and "mapped superclass" types.
 *
 * @author Steve Ebersole
 */
public interface IdentifiableJavaTypeDescriptor<T> extends ManagedJavaTypeDescriptor<T> {
	/**
	 * Overridden to further qualify the super-type
	 * <p/>
	 * {@inheritDoc}
	 */
	@Override
	IdentifiableJavaTypeDescriptor getSuperType();
}
