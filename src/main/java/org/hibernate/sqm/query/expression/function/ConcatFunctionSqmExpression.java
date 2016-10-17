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
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * Differs from {@link ConcatSqmExpression} in that
 * the function can have multiple arguments, whereas ConcatExpression only has 2.
 *
 * @see ConcatSqmExpression
 *
 * @author Steve Ebersole
 */
public class ConcatFunctionSqmExpression extends AbstractFunctionSqmExpression {
	public static final String NAME = "concat";

	private final List<SqmExpression> expressions;

	public ConcatFunctionSqmExpression(
			BasicType resultType,
			List<SqmExpression> expressions) {
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

	public List<SqmExpression> getExpressions() {
		return expressions;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitConcatFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "CONCAT(...)";
	}
}
