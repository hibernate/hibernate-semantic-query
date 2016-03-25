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
public class EmptinessSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression expression;

	public EmptinessSqmPredicate(SqmExpression expression) {
		this( expression, false );
	}

	public EmptinessSqmPredicate(SqmExpression expression, boolean negated) {
		super( negated );
		this.expression = expression;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitIsEmptyPredicate( this );
	}
}
