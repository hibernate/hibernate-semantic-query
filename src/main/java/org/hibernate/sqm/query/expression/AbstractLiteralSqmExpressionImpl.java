/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractLiteralSqmExpressionImpl<T> implements LiteralSqmExpression<T> {
	private final T value;
	private BasicType<T> typeDescriptor;

	public AbstractLiteralSqmExpressionImpl(T value, BasicType<T> typeDescriptor) {
		this.value = value;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public T getLiteralValue() {
		return value;
	}

	@Override
	public BasicType<T> getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public Type getInferableType() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(Type type) {
		if ( type != null ) {
			if ( !BasicType.class.isAssignableFrom( type.getClass() ) ) {
				throw new TypeInferenceException( "Inferred type descriptor [" + type + "] was not castable to javax.persistence.metamodel.BasicType" );
			}
			validateInferredType( ( (BasicType) type ).getJavaType() );
			this.typeDescriptor = (BasicType<T>) type;
		}
	}

	protected abstract void validateInferredType(Class javaType);
}
