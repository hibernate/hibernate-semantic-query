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
			String uid,
			String alias,
			EntityType entityTypeDescriptor) {
		super( fromElementSpace, uid, alias, entityTypeDescriptor, entityTypeDescriptor, alias );
	}

	public String getEntityName() {
		return getBoundModelType().getName();
	}

	@Override
	public EntityType getBoundModelType() {
		return (EntityType) super.getBoundModelType();
	}

	@Override
	public EntityType getIntrinsicSubclassIndicator() {
		// a root FromElement cannot indicate a subclass intrinsically (as part of its declaration)
		return null;
	}

	@Override
	public Attribute resolveAttribute(String attributeName) {
		return getBoundModelType().findAttribute( attributeName );
	}

	@Override
	public String toString() {
		return getEntityName() + " as " + getIdentificationVariable();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitRootEntityFromElement( this );
	}
}
