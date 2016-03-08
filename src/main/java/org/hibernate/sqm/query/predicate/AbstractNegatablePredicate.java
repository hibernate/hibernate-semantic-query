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
public abstract class AbstractNegatablePredicate implements NegatablePredicate {
	private boolean negated;

	public AbstractNegatablePredicate() {
		this( false );
	}

	public AbstractNegatablePredicate(boolean negated) {
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
