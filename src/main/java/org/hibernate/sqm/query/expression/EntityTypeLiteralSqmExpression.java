/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityDescriptor;

/**
 * Represents an reference to an entity type as a literal.  This is the JPA
 * terminology for cases when we have something like: {@code ... where TYPE(e) = SomeType}.
 * The token {@code SomeType} is an "entity type literal".
 *
 * @author Steve Ebersole
 */
public class EntityTypeLiteralSqmExpression implements SqmExpression {
	private final EntityDescriptor entityType;

	public EntityTypeLiteralSqmExpression(EntityDescriptor entityType) {
		this.entityType = entityType;
	}

	@Override
	public EntityDescriptor getExpressionType() {
		return entityType;
	}

	@Override
	public EntityDescriptor getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitEntityTypeLiteralExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "TYPE(" + entityType.getEntityName() + ")";
	}
}
