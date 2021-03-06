/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import org.hibernate.persister.queryable.spi.BasicValuedExpressableType;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class CountFunctionSqmExpression extends AbstractAggregateFunctionSqmExpression {
	public static final String NAME = "count";

	public CountFunctionSqmExpression(SqmExpression argument, boolean distinct, BasicValuedExpressableType resultType) {
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
