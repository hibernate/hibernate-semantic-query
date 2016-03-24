/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import org.hibernate.sqm.parser.criteria.spi.predicate.CriteriaPredicate;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * @author Steve Ebersole
 */
public interface PredicateImplementor extends CriteriaPredicate {
	/**
	 * Access to the CriteriaBuilder
	 *
	 * @return The CriteriaBuilder
	 */
	CriteriaBuilderImpl criteriaBuilder();

	/**
	 * Is this a conjunction or disjunction?
	 *
	 * @return {@code true} if this predicate is a junction (AND/OR); {@code false} otherwise
	 */
	boolean isJunction();
}
