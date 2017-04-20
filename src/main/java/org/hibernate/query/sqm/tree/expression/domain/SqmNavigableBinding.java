/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import java.util.Collection;

import org.hibernate.query.sqm.domain.SqmDomainTypeExporter;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.domain.SqmNavigable;
import org.hibernate.query.sqm.domain.type.SqmDomainType;
import org.hibernate.query.sqm.tree.SqmPropertyPath;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.from.SqmDowncast;

/**
 * Represents a particular {@link SqmNavigable} reference in regards to a SQM query.
 * E.g., a query defined as {@code select .. from Person p1, Person p2} has
 * 2 different NavigableBindings: the SqmRoot references p1 and p2
 *
 * @author Steve Ebersole
 */
public interface SqmNavigableBinding extends SqmExpression, SqmDomainTypeExporter {
	/**
	 * Get the binding for the lhs of the bound Navigable.
	 */
	SqmNavigableSourceBinding getSourceBinding();

	/**
	 * The Navigable represented by this binding.
	 */
	SqmNavigable getBoundNavigable();

	@Override
	default SqmDomainType getExportedDomainType() {
		return getBoundNavigable().getExportedDomainType();
	}

	/**
	 * Returns the property path that led to the creation of this Navigable.
	 */
	SqmPropertyPath getPropertyPath();


	// JPA downcast (TREAT .. AS ..) support

	SqmNavigableBinding treatAs(SqmExpressableTypeEntity target);

	void addDowncast(SqmDowncast downcast);

	Collection<SqmDowncast> getDowncasts();
}
