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
public class PositionalParameterSqmExpression implements ParameterSqmExpression {
	private final int position;
	private final boolean canBeMultiValued;
	private DomainReference typeDescriptor;

	public PositionalParameterSqmExpression(int position, boolean canBeMultiValued) {
		this.position = position;
		this.canBeMultiValued = canBeMultiValued;
	}

	public PositionalParameterSqmExpression(int position, boolean canBeMultiValued, DomainReference typeDescriptor) {
		this.position = position;
		this.canBeMultiValued = canBeMultiValued;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public DomainReference getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public DomainReference getInferableType() {
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitPositionalParameterExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "?" + getPosition();
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Integer getPosition() {
		return position;
	}

	@Override
	public void impliedType(DomainReference type) {
		if ( type != null ) {
			this.typeDescriptor = type;
		}
	}

	@Override
	public boolean allowMultiValuedBinding() {
		return canBeMultiValued;
	}

	@Override
	public DomainReference getAnticipatedType() {
		return getExpressionType();
	}
}
