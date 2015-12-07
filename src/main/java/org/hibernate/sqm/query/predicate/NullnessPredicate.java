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
public class NullnessPredicate implements NegatablePredicate {
	private final Expression expression;
	private final boolean negated;

	public NullnessPredicate(Expression expression) {
		this( expression, false );
	}

	public NullnessPredicate(Expression expression, boolean negated) {
		this.expression = expression;
		this.negated = negated;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitIsNullPredicate( this );
	}
}
