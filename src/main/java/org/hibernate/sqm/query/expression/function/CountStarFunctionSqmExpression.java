/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class CountStarFunctionSqmExpression extends AbstractAggregateFunctionSqmExpression {
	public CountStarFunctionSqmExpression(boolean distinct, BasicType resultType) {
		super( STAR, distinct, resultType );
	}

	@Override
	public String getFunctionName() {
		return CountFunctionSqmExpression.NAME;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCountStarFunction( this );
	}

	private static SqmExpression STAR = new SqmExpression() {
		@Override
		public Type getExpressionType() {
			return null;
		}

		@Override
		public Type getInferableType() {
			return null;
		}

		@Override
		public <T> T accept(SemanticQueryWalker<T> walker) {
			throw new UnsupportedOperationException( "Illegal attempt to visit * as argument of count(*)" );
		}
	};
}
