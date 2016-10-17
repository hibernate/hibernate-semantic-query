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
public class ConstantEnumSqmExpression<T extends Enum> implements ConstantSqmExpression<T> {
	private final T value;
	private DomainReference typeDescriptor;

	public ConstantEnumSqmExpression(T value) {
		this( value, null );
	}

	public ConstantEnumSqmExpression(T value, DomainReference typeDescriptor) {
		this.value = value;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public DomainReference getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public DomainReference getInferableType() {
		return getExpressionType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(DomainReference type) {
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
