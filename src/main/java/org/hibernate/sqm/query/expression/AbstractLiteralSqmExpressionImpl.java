/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.domain.Navigable;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractLiteralSqmExpressionImpl<T> implements LiteralSqmExpression<T> {
	private final T value;

	private Navigable type;

	public AbstractLiteralSqmExpressionImpl(T value) {
		this.value = value;
	}

	public AbstractLiteralSqmExpressionImpl(T value, Navigable type) {
		this.value = value;
	}

	@Override
	public T getLiteralValue() {
		return value;
	}

	@Override
	public Navigable getExpressionType() {
		return type;
	}

	@Override
	public Navigable getInferableType() {
		return getExpressionType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(Navigable type) {
		if ( type != null ) {
			this.type = type;
		}
	}

	@Override
	public String asLoggableText() {
		return "Literal( " + value + ")";
	}
}
