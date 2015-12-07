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
public class WhereClause {
	private Predicate predicate;

	public WhereClause() {
	}

	public WhereClause(Predicate predicate) {
		this.predicate = predicate;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public String toString() {
		return "where " + predicate;
	}
}
