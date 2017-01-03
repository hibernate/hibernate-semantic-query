/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.DomainTypeExporter;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.type.DomainType;
import org.hibernate.sqm.query.PropertyPath;

/**
 * Represents a particular {@link SqmNavigable} reference in regards to a SQM query.
 * E.g., a query defined as {@code select .. from Person p1, Person p2} has
 * 2 different NavigableBindings: the SqmRoot references p1 and p2
 *
 * @author Steve Ebersole
 */
public interface NavigableBinding extends DomainTypeExporter {
	/**
	 * Get the binding for the lhs of the bound Navigable.
	 */
	NavigableSourceBinding getSourceBinding();

	/**
	 * The Navigable represented by this binding.
	 */
	SqmNavigable getBoundNavigable();

	@Override
	default DomainType getExportedDomainType() {
		return getBoundNavigable().getExportedDomainType();
	}

	/**
	 * Returns the property path that led to the creation of this Navigable.
	 */
	PropertyPath getPropertyPath();
}
