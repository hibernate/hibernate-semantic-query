/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;

/**
 * @author Steve Ebersole
 */
public class NamedParameterSqmExpression implements ParameterSqmExpression {
	private final String name;
	private final boolean canBeMultiValued;
	private Navigable typeDescriptor;

	public NamedParameterSqmExpression(String name, boolean canBeMultiValued) {
		this.name = name;
		this.canBeMultiValued = canBeMultiValued;
	}

	public NamedParameterSqmExpression(String name, boolean canBeMultiValued, Navigable typeDescriptor) {
		this.name = name;
		this.canBeMultiValued = canBeMultiValued;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public Navigable getExpressionType() {
		return typeDescriptor;
	}

	@Override
	public Navigable getInferableType() {
		return null;
	}

	@Override
	public void impliedType(Navigable type) {
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
	public Navigable getAnticipatedType() {
		return getExpressionType();
	}
}
