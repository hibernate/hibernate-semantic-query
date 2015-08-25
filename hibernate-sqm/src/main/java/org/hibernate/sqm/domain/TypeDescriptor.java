/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.domain;

/**
 * Basic descriptor for a type in the user's domain model.
 *
 * @author Steve Ebersole
 */
public interface TypeDescriptor {
	/**
	 * The unique name for this type.
	 *
	 * @return The type name.
	 */
	String getTypeName();

	/**
	 * Look for a descriptor of the named attribute within this type.
	 *
	 * @param attributeName The name of the attribute for which to get the descriptor.
	 *
	 * @return The attribute's descriptor, or {@code null} if this type does not contain any
	 * such named attribute.
	 */
	AttributeDescriptor getAttributeDescriptor(String attributeName);
}
