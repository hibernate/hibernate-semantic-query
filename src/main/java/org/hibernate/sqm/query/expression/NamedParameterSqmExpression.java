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
public class NamedParameterSqmExpression implements ParameterSqmExpression {
	private final String name;
	private final boolean canBeMultiValued;
	private SqmExpressableType expressableType;

	public NamedParameterSqmExpression(String name, boolean canBeMultiValued) {
		this.name = name;
		this.canBeMultiValued = canBeMultiValued;
	}

	public NamedParameterSqmExpression(String name, boolean canBeMultiValued, SqmExpressableType expressableType) {
		this.name = name;
		this.canBeMultiValued = canBeMultiValued;
		this.expressableType = expressableType;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return expressableType;
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	@Override
	public void impliedType(SqmExpressableType expressableType) {
		if ( expressableType != null ) {
			this.expressableType = expressableType;
		}
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitNamedParameterExpression( this );
	}

	@Override
	public String asLoggableText() {
		return ":" + getName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getPosition() {
		return null;
	}

	@Override
	public boolean allowMultiValuedBinding() {
		return canBeMultiValued;
	}

	@Override
	public SqmExpressableType getAnticipatedType() {
		return getExpressionType();
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return getExpressionType().getExportedDomainType();
	}
}
