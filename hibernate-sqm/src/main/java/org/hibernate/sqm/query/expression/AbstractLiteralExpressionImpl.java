/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.domain.BasicTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractLiteralExpressionImpl<T> implements LiteralExpression<T> {
	private final T value;
	private final BasicTypeDescriptor typeDescriptor;

	public AbstractLiteralExpressionImpl(T value, BasicTypeDescriptor typeDescriptor) {
		this.value = value;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public T getLiteralValue() {
		return value;
	}

	@Override
	public BasicTypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}
}
