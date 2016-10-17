/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;

/**
 * @author Steve Ebersole
 */
public class NullifSqmExpression implements SqmExpression {
	private final SqmExpression first;
	private final SqmExpression second;

	public NullifSqmExpression(SqmExpression first, SqmExpression second) {
		this.first = first;
		this.second = second;
	}

	public SqmExpression getFirstArgument() {
		return first;
	}

	public SqmExpression getSecondArgument() {
		return second;
	}

	@Override
	public DomainReference getExpressionType() {
		return first.getExpressionType();
	}

	@Override
	public DomainReference getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitNullifExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "NULLIF(" + first.asLoggableText() + ", " + second.asLoggableText() + ")";
	}
}
