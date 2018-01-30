/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;

/**
 * @author Steve Ebersole
 */
public class ParameterizedEntityTypeSqmExpression implements SqmExpression {
	private final ParameterSqmExpression parameterExpression;

	public ParameterizedEntityTypeSqmExpression(ParameterSqmExpression parameterExpression) {
		this.parameterExpression = parameterExpression;
	}

	@Override
	public Navigable getExpressionType() {
		return parameterExpression.getExpressionType();
	}

	@Override
	public Navigable getInferableType() {
		return parameterExpression.getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitParameterizedEntityTypeExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "TYPE(" + parameterExpression.asLoggableText() + ")";
	}
}
