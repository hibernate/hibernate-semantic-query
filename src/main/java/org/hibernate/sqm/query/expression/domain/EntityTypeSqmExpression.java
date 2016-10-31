/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.query.expression.SqmExpression;

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
