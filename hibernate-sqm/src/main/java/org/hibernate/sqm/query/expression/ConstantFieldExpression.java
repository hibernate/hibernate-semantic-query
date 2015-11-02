/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * Represents a constant that came from a static field reference.
 * <p/>
 * TODO : would love to store a reference to the Field the value came from
 *
 * @author Steve Ebersole
 */
public class ConstantFieldExpression<T> implements ConstantExpression<T> {
	private final T value;
	private TypeDescriptor typeDescriptor;

	public ConstantFieldExpression(T value) {
		this( value, StandardBasicTypeDescriptors.INSTANCE.standardDescriptorForType( value.getClass() ) );
	}

	public ConstantFieldExpression(T value, TypeDescriptor typeDescriptor) {
		this.value = value;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitConstantFieldExpression( this );
	}
}
