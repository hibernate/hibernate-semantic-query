/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import org.hibernate.query.sqm.domain.type.SqmDomainType;
import org.hibernate.query.sqm.domain.SqmExpressableType;

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
