/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import org.hibernate.persister.queryable.spi.BasicValuedExpressableType;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAggregateFunctionSqmExpression
		extends AbstractFunctionSqmExpression
		implements AggregateFunctionSqmExpression {
	private final SqmExpression argument;
	private final boolean distinct;

	public AbstractAggregateFunctionSqmExpression(SqmExpression argument, boolean distinct, BasicValuedExpressableType resultType) {
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
