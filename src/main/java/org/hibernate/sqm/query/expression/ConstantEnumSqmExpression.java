/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;

/**
 * @author Steve Ebersole
 */
public class ConstantEnumSqmExpression<T extends Enum> implements ConstantSqmExpression<T> {
	private final T value;
	private Navigable typeDescriptor;

	public ConstantEnumSqmExpression(T value) {
		this( value, null );
	}

	public ConstantEnumSqmExpression(T value, Navigable typeDescriptor) {
		this.value = value;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public Navigable getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public Navigable getInferableType() {
		return getExpressionType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(Navigable type) {
		this.typeDescriptor = type;
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitConstantEnumExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "EnumConstant(" + value + ")";
	}
}
