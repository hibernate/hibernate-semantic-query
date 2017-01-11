/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.query.predicate.SqmPredicate;

/**
 * Common contract for qualified/restricted/predicated joins.
 *
 * @author Steve Ebersole
 */
public interface SqmQualifiedJoin extends SqmJoin {
	/**
	 * Obtain the join predicate
	 *
	 * @return The join predicate
	 */
	SqmPredicate getOnClausePredicate();

	/**
	 * Inject the join predicate
	 *
	 * @param predicate The join predicate
	 */
	void setOnClausePredicate(SqmPredicate predicate);

	// todo : specialized Predicate for "mapped attribute join" conditions
}
