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
public class CountFunctionSqmExpression extends AbstractAggregateFunctionSqmExpression {
	public static final String NAME = "count";

	public CountFunctionSqmExpression(SqmExpression argument, boolean distinct, Navigable resultType) {
		super( argument, distinct, resultType );
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCountFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "COUNT(" + getArgument().asLoggableText() + ")";
	}
}
