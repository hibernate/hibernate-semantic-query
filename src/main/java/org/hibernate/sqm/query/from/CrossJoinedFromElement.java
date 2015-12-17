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
import org.hibernate.sqm.query.JoinType;

/**
 * @author Steve Ebersole
 */
public class CrossJoinedFromElement extends AbstractFromElement implements JoinedFromElement {

	public CrossJoinedFromElement(
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
	public JoinType getJoinType() {
		return JoinType.CROSS;
	}

	@Override
	public Attribute resolveAttribute(String attributeName) {
		return getBoundModelType().findAttribute( attributeName );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCrossJoinedFromElement( this );
	}
}
