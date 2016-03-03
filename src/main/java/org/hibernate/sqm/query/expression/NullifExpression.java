/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class NullifExpression implements Expression {
	private final Expression first;
	private final Expression second;

	public NullifExpression(Expression first, Expression second) {
		this.first = first;
		this.second = second;
	}

	public Expression getFirstArgument() {
		return first;
	}

	public Expression getSecondArgument() {
		return second;
	}

	@Override
	public Type getExpressionType() {
		return first.getExpressionType();
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitNullifExpression( this );
	}
}
