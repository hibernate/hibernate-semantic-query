/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.SubQueryExpression;

/**
 * @author Steve Ebersole
 */
public class InSubQueryPredicate implements InPredicate {
	private final Expression testExpression;
	private final SubQueryExpression subQueryExpression;
	private final boolean negated;

	public InSubQueryPredicate(
			Expression testExpression,
			SubQueryExpression subQueryExpression) {
		this( testExpression, subQueryExpression, false );
	}

	public InSubQueryPredicate(
			Expression testExpression,
			SubQueryExpression subQueryExpression, boolean negated) {
		this.testExpression = testExpression;
		this.subQueryExpression = subQueryExpression;
		this.negated = negated;
	}

	@Override
	public Expression getTestExpression() {
		return testExpression;
	}

	public SubQueryExpression getSubQueryExpression() {
		return subQueryExpression;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInSubQueryPredicate( this );
	}
}
