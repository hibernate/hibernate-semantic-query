/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public class ParameterizedEntityTypeSqmExpression implements SqmExpression {
	private final ParameterSqmExpression parameterExpression;

	public ParameterizedEntityTypeSqmExpression(ParameterSqmExpression parameterExpression) {
		this.parameterExpression = parameterExpression;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return parameterExpression.getExpressionType();
	}

	@Override
	public SqmExpressableType getInferableType() {
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

	@Override
	public SqmDomainType getExportedDomainType() {
		return getExpressionType().getExportedDomainType();
	}
}
