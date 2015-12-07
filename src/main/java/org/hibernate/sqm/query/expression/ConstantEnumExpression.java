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
 * @author Steve Ebersole
 */
public class ConstantEnumExpression<T extends Enum> implements ConstantExpression<T> {
	private final T value;
	private BasicType<T> typeDescriptor;

	public ConstantEnumExpression(T value, BasicType<T> typeDescriptor) {
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
			BasicType basicType = (BasicType) type;
			if ( !value.getClass().equals( basicType.getJavaType() ) ) {
				throw new TypeInferenceException( "Inferred type [" + basicType.getJavaType() + "] was not convertible to " + value.getClass().getName() );
			}
			this.typeDescriptor = basicType;
		}
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitConstantEnumExpression( this );
	}
}
