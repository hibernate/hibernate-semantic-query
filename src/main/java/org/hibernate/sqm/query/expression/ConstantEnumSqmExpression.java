/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public class ConstantEnumSqmExpression<T extends Enum> implements ConstantSqmExpression<T> {
	private final T value;
	private SqmExpressableType domainType;

	public ConstantEnumSqmExpression(T value) {
		this( value, null );
	}

	public ConstantEnumSqmExpression(T value, SqmExpressableType domainType) {
		this.value = value;
		this.domainType = domainType;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return domainType;
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(SqmExpressableType expressableType) {
		this.domainType = domainType;
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitConstantEnumExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "EnumConstant(" + value + ")";
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return getExpressionType().getExportedDomainType();
	}
}
