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
import org.hibernate.sqm.query.predicate.SqmPredicate;

/**
 * @author Steve Ebersole
 */
public class SqmEntityJoin
		extends AbstractJoin
		implements SqmQualifiedJoin {
	private final String entityName;

	private SqmPredicate onClausePredicate;

	public SqmEntityJoin(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityType joinedEntityDescriptor,
			JoinType joinType) {
		super( fromElementSpace, uid, alias, joinedEntityDescriptor, joinedEntityDescriptor, alias, joinType );
		this.entityName = joinedEntityDescriptor.getName();
	}

	public String getEntityName() {
		return entityName;
	}

	@Override
	public EntityType getBindable() {
		return (EntityType) super.getBindable();
	}

	@Override
	public Attribute resolveAttribute(String attributeName) {
		return getBindable().findAttribute( attributeName );
	}

	@Override
	public SqmPredicate getOnClausePredicate() {
		return onClausePredicate;
	}

	public void setOnClausePredicate(SqmPredicate predicate) {
		this.onClausePredicate = predicate;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitQualifiedEntityJoinFromElement( this );
	}
}
