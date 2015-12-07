/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;

/**
 * Represents a constant that came from a static field reference.
 * <p/>
 * TODO : would love to store a reference to the Field the value came from
 *
 * @author Steve Ebersole
 */
public class ConstantFieldExpression<T> implements ConstantExpression<T> {
	private final T value;
	private BasicType<T> typeDescriptor;

	public ConstantFieldExpression(T value, BasicType<T> typeDescriptor) {
		this.value = value;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public BasicType<T> getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(Type type) {
		if ( type != null ) {
			if ( !BasicType.class.isAssignableFrom( type.getClass() ) ) {
				throw new TypeInferenceException( "Inferred type descriptor [" + type + "] was not castable to javax.persistence.metamodel.BasicType" );
			}
			final BasicType basicType = (BasicType) type;
			if ( !value.getClass().equals( basicType.getJavaType() ) ) {
				throw new TypeInferenceException( "Inferred type [" + basicType.getJavaType() + "] was not convertible to " + value.getClass().getName() );
			}
			this.typeDescriptor = basicType;
		}
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitConstantFieldExpression( this );
	}
}
