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
public abstract class AbstractNegatableSqmPredicate implements NegatableSqmPredicate {
	private boolean negated;

	public AbstractNegatableSqmPredicate() {
		this( false );
	}

	public AbstractNegatableSqmPredicate(boolean negated) {
		this.negated = negated;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public void negate() {
		this.negated = !this.negated;
	}
}
