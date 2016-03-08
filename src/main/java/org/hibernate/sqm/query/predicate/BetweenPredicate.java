/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class BetweenPredicate extends AbstractNegatablePredicate {
	private final Expression expression;
	private final Expression lowerBound;
	private final Expression upperBound;

	public BetweenPredicate(
			Expression expression,
			Expression lowerBound,
			Expression upperBound,
			boolean negated) {
		super( negated );
		this.expression = expression;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public Expression getExpression() {
		return expression;
	}

	public Expression getLowerBound() {
		return lowerBound;
	}

	public Expression getUpperBound() {
		return upperBound;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBetweenPredicate( this );
	}
}
