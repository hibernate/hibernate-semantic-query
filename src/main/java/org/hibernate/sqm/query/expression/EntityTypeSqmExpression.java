/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.parser.common.DomainReferenceBinding;

/**
 * @author Steve Ebersole
 */
public class EntityTypeSqmExpression implements SqmExpression {
	private final DomainReferenceBinding binding;

	public EntityTypeSqmExpression(DomainReferenceBinding binding) {
		this.binding = binding;
	}

	public DomainReferenceBinding getBinding() {
		return binding;
	}

	@Override
	public DomainReference getExpressionType() {
		return binding.getBoundDomainReference();
	}

	@Override
	public DomainReference getInferableType() {
		return binding.getBoundDomainReference();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitEntityTypeExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "TYPE(" + binding.asLoggableText() + ")";
	}
}
