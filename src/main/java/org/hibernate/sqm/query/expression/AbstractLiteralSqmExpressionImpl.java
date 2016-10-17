/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.domain.DomainReference;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractLiteralSqmExpressionImpl<T> implements LiteralSqmExpression<T> {
	private final T value;

	private DomainReference type;

	public AbstractLiteralSqmExpressionImpl(T value) {
		this.value = value;
	}

	public AbstractLiteralSqmExpressionImpl(T value, DomainReference type) {
		this.value = value;
	}

	@Override
	public T getLiteralValue() {
		return value;
	}

	@Override
	public DomainReference getExpressionType() {
		return type;
	}

	@Override
	public DomainReference getInferableType() {
		return getExpressionType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(DomainReference type) {
		if ( type != null ) {
			this.type = type;
		}
	}

	@Override
	public String asLoggableText() {
		return "Literal( " + value + ")";
	}
}
