/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.domain.Navigable;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractFunctionSqmExpression implements FunctionSqmExpression {
	private final Navigable resultType;

	public AbstractFunctionSqmExpression(Navigable resultType) {
		this.resultType = resultType;
	}

	@Override
	public Navigable getFunctionResultType() {
		return resultType;
	}

	@Override
	public Navigable getExpressionType() {
		return getFunctionResultType();
	}

	@Override
	public Navigable getInferableType() {
		return getFunctionResultType();
	}
}
