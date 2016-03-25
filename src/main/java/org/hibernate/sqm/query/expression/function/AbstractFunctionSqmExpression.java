/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.domain.BasicType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractFunctionSqmExpression implements FunctionSqmExpression {
	private final BasicType resultType;

	public AbstractFunctionSqmExpression(BasicType resultType) {
		this.resultType = resultType;
	}

	@Override
	public BasicType getFunctionResultType() {
		return resultType;
	}

	@Override
	public BasicType getExpressionType() {
		return getFunctionResultType();
	}

	@Override
	public BasicType getInferableType() {
		return getFunctionResultType();
	}
}
