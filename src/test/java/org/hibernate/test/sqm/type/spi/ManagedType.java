/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.spi;

import org.hibernate.sqm.domain.type.DomainType;

/**
 * Contract for "managed types", which is taken from the JPA term for commonality between
 * entity, embeddable and "mapped superclass" types.
 *
 * @author Steve Ebersole
 */
public interface ManagedType extends Type, DomainType, javax.persistence.metamodel.ManagedType {
	/**
	 * Obtain the super-type for this type.
	 * <p/>
	 * Note that for embeddables this is currently no-op, so at the moment really only
	 * IdentifiableType implementations can have a super-type.  But Hibernate does have plans
	 * to add support for embeddable inheritance, so this is here for future compatibility.
	 *
	 * @return The type's super-type
	 */
	ManagedType getSuperType();

	/**
	 * Find the named attribute.  This form considers attributes defined on any super-types
	 * as well, as opposed to {@link #findDeclaredAttribute(String)} which does not.
	 *
	 * @param name The attribute name.
	 *
	 * @return The matching attribute, or {@code null} if none found.
	 */
	Attribute findAttribute(String name);

	/**
	 * Find the named declared attribute.  This form considers only attributes declared on this type,
	 * and therefore does not include any attributes defined on any of its super-types.
	 *
	 * @param name The attribute name.
	 *
	 * @return The matching attribute, or {@code null} if none found.
	 *
	 * @todo : is this form really needed in SQM?  We'd generally always access attributes via #findAttribute and see if it is declared against the type.
	 * 		^^ maybe when inside of a TREAT as an extra form of validation?
	 */
	Attribute findDeclaredAttribute(String name);
}
