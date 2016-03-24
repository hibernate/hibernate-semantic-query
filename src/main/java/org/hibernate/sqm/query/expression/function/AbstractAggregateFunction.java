/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAggregateFunction extends AbstractFunctionExpression implements AggregateFunction {
	private final Expression argument;
	private final boolean distinct;

	public AbstractAggregateFunction(Expression argument, boolean distinct, BasicType resultType) {
		super( resultType );
		this.argument = argument;
		this.distinct = distinct;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public Expression getArgument() {
		return argument;
	}

	@Override
	public boolean isDistinct() {
		return distinct;
	}
}
