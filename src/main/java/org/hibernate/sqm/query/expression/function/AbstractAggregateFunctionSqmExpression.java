/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAggregateFunctionSqmExpression
		extends AbstractFunctionSqmExpression
		implements AggregateFunctionSqmExpression {
	private final SqmExpression argument;
	private final boolean distinct;

	public AbstractAggregateFunctionSqmExpression(SqmExpression argument, boolean distinct, SqmExpressableType resultType) {
		super( resultType );
		this.argument = argument;
		this.distinct = distinct;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public SqmExpression getArgument() {
		return argument;
	}

	@Override
	public boolean isDistinct() {
		return distinct;
	}
}
