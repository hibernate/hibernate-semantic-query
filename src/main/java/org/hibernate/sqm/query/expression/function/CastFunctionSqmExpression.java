/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class CastFunctionSqmExpression extends AbstractFunctionSqmExpression implements FunctionSqmExpression {
	public static final String NAME = "cast";

	private final SqmExpression expressionToCast;

	public CastFunctionSqmExpression(SqmExpression expressionToCast, SqmExpressableTypeBasic castTargetType) {
		super( castTargetType );
		this.expressionToCast = expressionToCast;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	public SqmExpression getExpressionToCast() {
		return expressionToCast;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCastFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "CAST(" + expressionToCast.asLoggableText() + ")";
	}
}
