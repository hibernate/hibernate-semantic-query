/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;

/**
 * @author Steve Ebersole
 */
public class GroupedPredicate implements Predicate {
	private final Predicate subPredicate;

	public GroupedPredicate(Predicate subPredicate) {
		this.subPredicate = subPredicate;
	}

	public Predicate getSubPredicate() {
		return subPredicate;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitGroupedPredicate( this );
	}
}
