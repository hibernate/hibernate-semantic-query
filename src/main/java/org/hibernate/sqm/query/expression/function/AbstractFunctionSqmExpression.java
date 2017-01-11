/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractFunctionSqmExpression implements FunctionSqmExpression {
	private final SqmExpressableType resultType;

	public AbstractFunctionSqmExpression(SqmExpressableType resultType) {
		this.resultType = resultType;
	}

	@Override
	public SqmExpressableType getFunctionResultType() {
		return resultType;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return getFunctionResultType();
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getFunctionResultType();
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return getFunctionResultType().getExportedDomainType();
	}
}
