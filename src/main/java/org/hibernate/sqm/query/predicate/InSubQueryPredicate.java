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
public class InSubQueryPredicate extends AbstractNegatablePredicate implements InPredicate {
	private final Expression testExpression;
	private final SubQueryExpression subQueryExpression;

	public InSubQueryPredicate(
			Expression testExpression,
			SubQueryExpression subQueryExpression) {
		this( testExpression, subQueryExpression, false );
	}

	public InSubQueryPredicate(
			Expression testExpression,
			SubQueryExpression subQueryExpression,
			boolean negated) {
		super( negated );
		this.testExpression = testExpression;
		this.subQueryExpression = subQueryExpression;
	}

	@Override
	public Expression getTestExpression() {
		return testExpression;
	}

	public SubQueryExpression getSubQueryExpression() {
		return subQueryExpression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInSubQueryPredicate( this );
	}
}
