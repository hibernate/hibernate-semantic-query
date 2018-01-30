/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class AvgFunctionSqmExpression
		extends AbstractAggregateFunctionSqmExpression
		implements AggregateFunctionSqmExpression {
	public static final String NAME = "avg";

	public AvgFunctionSqmExpression(SqmExpression argument, boolean distinct, Navigable resultType) {
		super( argument, distinct, resultType );
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAvgFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "AVG(" + getArgument().asLoggableText() + ")";
	}
}
