/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class LowerFunctionSqmExpression extends AbstractFunctionSqmExpression {
	public static final String NAME = "lower";

	private SqmExpression expression;

	public LowerFunctionSqmExpression(Navigable resultType, SqmExpression expression) {
		super( resultType );
		this.expression = expression;

		assert expression != null;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLowerFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "LOWER(" + getExpression().asLoggableText() + ")";
	}
}
