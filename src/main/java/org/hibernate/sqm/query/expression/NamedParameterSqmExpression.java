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
public class NamedParameterSqmExpression implements ParameterSqmExpression {
	private final String name;
	private final boolean canBeMultiValued;
	private DomainReference typeDescriptor;

	public NamedParameterSqmExpression(String name, boolean canBeMultiValued) {
		this.name = name;
		this.canBeMultiValued = canBeMultiValued;
	}

	public NamedParameterSqmExpression(String name, boolean canBeMultiValued, DomainReference typeDescriptor) {
		this.name = name;
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
	public void impliedType(DomainReference type) {
		if ( type != null ) {
			this.typeDescriptor = type;
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
	public DomainReference getAnticipatedType() {
		return getExpressionType();
	}
}
