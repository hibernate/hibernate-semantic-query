/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class BetweenSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression expression;
	private final SqmExpression lowerBound;
	private final SqmExpression upperBound;

	public BetweenSqmPredicate(
			SqmExpression expression,
			SqmExpression lowerBound,
			SqmExpression upperBound,
			boolean negated) {
		super( negated );
		this.expression = expression;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	public SqmExpression getLowerBound() {
		return lowerBound;
	}

	public SqmExpression getUpperBound() {
		return upperBound;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBetweenPredicate( this );
	}
}
