/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityReference;

/**
 * Represents an reference to an entity type
 *
 * @author Steve Ebersole
 */
public class EntityTypeSqmExpression implements SqmExpression {
	private final EntityReference entityType;

	public EntityTypeSqmExpression(EntityReference entityType) {
		this.entityType = entityType;
	}

	@Override
	public EntityReference getExpressionType() {
		return entityType;
	}

	@Override
	public EntityReference getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitEntityTypeExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "TYPE(" + entityType.getEntityName() + ")";
	}
}
