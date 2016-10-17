/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.lang.reflect.Field;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;

/**
 * Represents a constant that came from a static field reference.
 *
 * @author Steve Ebersole
 */
public class ConstantFieldSqmExpression<T> implements ConstantSqmExpression<T> {
	private final Field sourceField;
	private final T value;

	private DomainReference typeDescriptor;

	public ConstantFieldSqmExpression(Field sourceField, T value) {
		this( sourceField, value, null );
	}

	public ConstantFieldSqmExpression(Field sourceField, T value, DomainReference typeDescriptor) {
		this.sourceField = sourceField;
		this.value = value;
		this.typeDescriptor = typeDescriptor;
	}

	public Field getSourceField() {
		return sourceField;
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
		if ( type != null ) {
			this.typeDescriptor = type;
		}
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitConstantFieldExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "ConstantField(" + value + ")";
	}
}
