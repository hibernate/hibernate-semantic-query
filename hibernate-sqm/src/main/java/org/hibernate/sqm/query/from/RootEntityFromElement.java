/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;

/**
 * @author Steve Ebersole
 */
public class RootEntityFromElement extends AbstractFromElement {
	public RootEntityFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			EntityType entityTypeDescriptor) {
		super( fromElementSpace, alias, entityTypeDescriptor );
	}

	public String getEntityName() {
		return getBindableModelDescriptor().getName();
	}

	@Override
	public EntityType getBindableModelDescriptor() {
		return (EntityType) super.getBindableModelDescriptor();
	}

	@Override
	public Attribute resolveAttribute(String attributeName) {
		return getBindableModelDescriptor().findAttribute( attributeName );
	}

	@Override
	public String toString() {
		return getEntityName() + " as " + getAlias();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new UnsupportedOperationException( "see todo.md comment" );
	}
}
