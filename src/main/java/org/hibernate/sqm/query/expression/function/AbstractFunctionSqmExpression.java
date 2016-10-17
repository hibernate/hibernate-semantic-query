/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.domain.DomainReference;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractFunctionSqmExpression implements FunctionSqmExpression {
	private final DomainReference resultType;

	public AbstractFunctionSqmExpression(DomainReference resultType) {
		this.resultType = resultType;
	}

	@Override
	public DomainReference getFunctionResultType() {
		return resultType;
	}

	@Override
	public DomainReference getExpressionType() {
		return getFunctionResultType();
	}

	@Override
	public DomainReference getInferableType() {
		return getFunctionResultType();
	}
}
