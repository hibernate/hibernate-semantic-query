/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.path.FromElementBinding;

/**
 * @author Steve Ebersole
 */
public interface FromElement extends FromElementBinding, Downcastable {
	/**
	 * A unique identifier across all QuerySpecs (all AliasRegistry instances) for a given sqm.
	 * <p/>
	 * Can be used to locate a FromElement outside the context of a particular AliasRegistry.
	 *
	 * @return This FromElement's unique identifier
	 *
	 * @see ParsingContext#globalFromElementMap
	 */
	String getUniqueIdentifier();

	/**
	 * Obtain reference to the FromElementSpace that this FromElement belongs to.
	 *
	 * @return The FromElementSpace containing this FromElement
	 */
	FromElementSpace getContainingSpace();

	/**
	 * Obtain the Bindable referenced by this FromElement.
	 *
	 * @return The bound type (Bindable)
	 */
	Bindable getBoundModelType();

	/**
	 * Obtain the downcast target for cases where a downcast (treat) is defined in the
	 * directly in the from-clause where this FromElement is declared.  E.g
	 * <code>select b.isbn from Order o join treat(o.product as Book b)</code>; here
	 * the FromElement indicated by {@code join treat(o.product as Book b)} would have
	 * Book as an intrinsic subclass indicator.
	 *
	 * @return The subclass indicated directly as part of the FromElement's declaration;
	 * will return {@code null} for FromElement declarations that do not directly specify
	 * a downcast.
	 *
	 * @see org.hibernate.sqm.path.AttributeBindingSource#getSubclassIndicator()
	 */
	EntityType getIntrinsicSubclassIndicator();

	/**
	 * Get the identification variable (alias) assigned to this FromElement.  If an explicit
	 * identification variable was given in the source sqm that identification variable is
	 * returned here; otherwise an implicit identification variable is generated and returned
	 * here.
	 * <p/>
	 * Note that the spec also sometimes calls this a "range variable", although it tends to
	 * limit this usage to just sqm space roots.
	 *
	 * @return The identification variable (alias) for this FromElement.  Never returns
	 * {@code null}; if the sqm did not specify an identification variable, one is implicitly
	 * generated.
	 */
	String getIdentificationVariable();

	/**
	 * TODO : Remove this?
	 *
	 * @param attributeName
	 * @return
	 */
	Attribute resolveAttribute(String attributeName);
}
