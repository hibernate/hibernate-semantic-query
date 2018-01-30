/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.domain.EntityReference;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.predicate.SqmPredicate;

/**
 * @author Steve Ebersole
 */
public class SqmEntityJoin
		extends AbstractJoin
		implements SqmQualifiedJoin {
	private SqmPredicate onClausePredicate;

	public SqmEntityJoin(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityDescriptor joinedEntityDescriptor,
			JoinType joinType) {
		super(
				fromElementSpace,
				uid,
				alias,
				new EntityReference( joinedEntityDescriptor ),
				joinedEntityDescriptor,
				new PropertyPath( null, joinedEntityDescriptor.getEntityName() + "(" + alias + ")" ),
				joinType
		);
		getDomainReferenceBinding().injectFromElement( this );
	}

	@Override
	public EntityReference getDomainReferenceBinding() {
		return (EntityReference) super.getDomainReferenceBinding();
	}

	public String getEntityName() {
		return getDomainReferenceBinding().getBoundDomainReference().getEntityName();
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
