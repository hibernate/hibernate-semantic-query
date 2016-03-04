/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.path;

import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.query.from.FromElement;

/**
 * A source for an AttributeBinding.  This represents the "left-hand side" of a attribute
 * reference - its source.
 *
 * @author Steve Ebersole
 */
public interface AttributeBindingSource extends Binding {
	/**
	 * Obtain reference to the underlying FromElement that this
	 * AttributeBindingSource points to.
	 *
	 * @return The FromElement that backs this AttributeBindingSource
	 */
	FromElement getFromElement();

	/**
	 * Obtain access to the ManagedTyped that corresponds to the bound type
	 * indicated by {@link #getBoundModelType()}
	 *
	 * @return The corresponding ManagedTyped (for access to Attribute definitions)
	 */
	ManagedType getAttributeContributingType();

	/**
	 * Which specific subclass of the ManagedType ({@link #getAttributeContributingType})
	 * is indicated here?
	 *
	 * @return The specific subclass.
	 */
	ManagedType getSubclassIndicator();
}
