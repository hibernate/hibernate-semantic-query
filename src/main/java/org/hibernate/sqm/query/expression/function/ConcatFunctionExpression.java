/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.query.expression.Expression;

/**
 * Differs from {@link org.hibernate.sqm.query.expression.ConcatExpression} in that
 * the function can have multiple arguments, whereas ConcatExpression only has 2.
 *
 * @see org.hibernate.sqm.query.expression.ConcatExpression
 *
 * @author Steve Ebersole
 */
public class ConcatFunctionExpression extends AbstractFunctionExpression {
	public static final String NAME = "concat";

	private final List<Expression> expressions;

	public ConcatFunctionExpression(
			BasicType resultType,
			List<Expression> expressions) {
		super( resultType );
		this.expressions = expressions;

		assert expressions != null;
		assert expressions.size() >= 2;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	public List<Expression> getExpressions() {
		return expressions;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitConcatFunction( this );
	}
}
