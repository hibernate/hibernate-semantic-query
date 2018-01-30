/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.paths.internal;

import org.hibernate.query.sqm.produce.SqmProductionException;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;

/**
 * @author Steve Ebersole
 */
public class SemanticPathPartNamedEntity implements SemanticPathPart {
	private final EntityDescriptor entityDescriptor;

	public SemanticPathPartNamedEntity(EntityDescriptor entityDescriptor) {
		this.entityDescriptor = entityDescriptor;
	}

	public EntityDescriptor getEntityDescriptor() {
		return entityDescriptor;
	}


	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SqmProductionException( "Cannot dereference an entity name" );
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SqmProductionException( "Cannot dereference an entity name" );
	}
}
