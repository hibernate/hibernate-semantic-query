/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class MaxFunction extends AbstractAggregateFunction implements AggregateFunction {
	public MaxFunction(Expression argument, boolean distinct, BasicType resultType) {
		super( argument, distinct, resultType );
	}

	@Override
	public BasicType getExpressionType() {
		return (BasicType) super.getExpressionType();
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMaxFunction( this );
	}
}
