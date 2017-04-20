/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEntity;

/**
 * Represents an reference to an entity type as a literal.  This is the JPA
 * terminology for cases when we have something like: {@code ... where TYPE(e) = SomeType}.
 * The token {@code SomeType} is an "entity type literal".
 *
 * @author Steve Ebersole
 */
public class EntityTypeLiteralSqmExpression implements SqmExpression {
	private final SqmExpressableTypeEntity entityType;

	public EntityTypeLiteralSqmExpression(SqmExpressableTypeEntity entityType) {
		this.entityType = entityType;
	}

	@Override
	public SqmExpressableTypeEntity getExpressionType() {
		return entityType;
	}

	@Override
	public SqmExpressableTypeEntity getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitEntityTypeLiteralExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "TYPE(" + entityType + ")";
	}

	@Override
	public SqmDomainTypeEntity getExportedDomainType() {
		return (SqmDomainTypeEntity) getExpressionType().getExportedDomainType();
	}
}
