/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;

/**
 * @author Steve Ebersole
 */
public class NegatedPredicate implements Predicate {
	private final Predicate wrappedPredicate;

	public NegatedPredicate(Predicate wrappedPredicate) {
		this.wrappedPredicate = wrappedPredicate;
	}

	public Predicate getWrappedPredicate() {
		return wrappedPredicate;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitNegatedPredicate( this );
	}
}
