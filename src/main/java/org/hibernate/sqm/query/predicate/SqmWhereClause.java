/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

/**
 * @author Steve Ebersole
 */
public class SqmWhereClause {
	private SqmPredicate predicate;

	public SqmWhereClause() {
	}

	public SqmWhereClause(SqmPredicate predicate) {
		this.predicate = predicate;
	}

	public SqmPredicate getPredicate() {
		return predicate;
	}

	public void setPredicate(SqmPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public String toString() {
		return "where " + predicate;
	}
}
