/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class CoalesceExpression implements Expression {
	private List<Expression> values = new ArrayList<Expression>();

	public List<Expression> getValues() {
		return values;
	}

	public void value(Expression expression) {
		values.add( expression );
	}

	@Override
	public Type getExpressionType() {
		return values.get( 0 ).getExpressionType();
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCoalesceExpression( this );
	}
}
