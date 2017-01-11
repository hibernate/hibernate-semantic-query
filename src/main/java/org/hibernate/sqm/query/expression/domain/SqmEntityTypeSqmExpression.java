/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * Expression representing the type of an entity-valued expression.
 * E.g. a {@code TYPE(path)} expression
 *
 * @author Steve Ebersole
 */
public class SqmEntityTypeSqmExpression implements SqmExpression {
	private final SqmNavigableBinding binding;

	public SqmEntityTypeSqmExpression(SqmNavigableBinding binding) {
		this.binding = binding;
	}

	public SqmNavigableBinding getBinding() {
		return binding;
	}

	@Override
	public SqmNavigable getExpressionType() {
		return binding.getBoundNavigable();
	}

	@Override
	public SqmNavigable getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitEntityTypeExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "TYPE(" + binding.asLoggableText() + ")";
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return getExpressionType().getExportedDomainType();
	}
}
