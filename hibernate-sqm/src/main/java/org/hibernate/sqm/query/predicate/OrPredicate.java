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
public class OrPredicate implements Predicate {
	private final Predicate leftHandPredicate;
	private final Predicate rightHandPredicate;

	public OrPredicate(Predicate leftHandPredicate, Predicate rightHandPredicate) {
		this.leftHandPredicate = leftHandPredicate;
		this.rightHandPredicate = rightHandPredicate;
	}

	public Predicate getLeftHandPredicate() {
		return leftHandPredicate;
	}

	public Predicate getRightHandPredicate() {
		return rightHandPredicate;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitOrPredicate( this );
	}
}
