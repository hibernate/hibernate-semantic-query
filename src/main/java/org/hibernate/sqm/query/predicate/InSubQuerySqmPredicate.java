/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;

/**
 * @author Steve Ebersole
 */
public class InSubQuerySqmPredicate extends AbstractNegatableSqmPredicate implements InSqmPredicate {
	private final SqmExpression testExpression;
	private final SubQuerySqmExpression subQueryExpression;

	public InSubQuerySqmPredicate(
			SqmExpression testExpression,
			SubQuerySqmExpression subQueryExpression) {
		this( testExpression, subQueryExpression, false );
	}

	public InSubQuerySqmPredicate(
			SqmExpression testExpression,
			SubQuerySqmExpression subQueryExpression,
			boolean negated) {
		super( negated );
		this.testExpression = testExpression;
		this.subQueryExpression = subQueryExpression;
	}

	@Override
	public SqmExpression getTestExpression() {
		return testExpression;
	}

	public SubQuerySqmExpression getSubQueryExpression() {
		return subQueryExpression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInSubQueryPredicate( this );
	}
}
