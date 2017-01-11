/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.domain.SqmExpressableType;
import org.hibernate.sqm.parser.SemanticException;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractLiteralSqmExpressionImpl<T> implements LiteralSqmExpression<T> {
	private final T value;

	private SqmExpressableTypeBasic type;

	public AbstractLiteralSqmExpressionImpl(T value) {
		this.value = value;
	}

	public AbstractLiteralSqmExpressionImpl(T value, SqmExpressableTypeBasic type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T getLiteralValue() {
		return value;
	}

	@Override
	public SqmExpressableTypeBasic getExpressionType() {
		return type;
	}

	@Override
	public SqmExpressableTypeBasic getInferableType() {
		return getExpressionType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(SqmExpressableType type) {
		if ( type != null ) {
			if ( !SqmExpressableTypeBasic.class.isInstance( type ) ) {
				throw new SemanticException( "Inferrable type for literal was found to be a non-basic value : " + type );
			}
			this.type = (SqmExpressableTypeBasic) type;
		}
	}

	@Override
	public String asLoggableText() {
		return "Literal( " + value + ")";
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return type.getExportedDomainType();
	}
}
