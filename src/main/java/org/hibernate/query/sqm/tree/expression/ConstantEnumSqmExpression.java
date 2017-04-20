/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.type.SqmDomainType;
import org.hibernate.query.sqm.domain.SqmExpressableType;

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
