/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.spi;

/**
 * Represents an attribute in the domain model.
 *
 * @author Steve Ebersole
 */
public interface Attribute extends javax.persistence.metamodel.Attribute {
	/**
	 * Obtain the type that declares the attribute.
	 */
	ManagedType getDeclaringType();
}
