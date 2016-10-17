/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.parser.common.DomainReferenceBinding;
import org.hibernate.sqm.parser.common.EntityBinding;
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
			EntityReference joinedEntityDescriptor,
			JoinType joinType) {
		super(
				fromElementSpace,
				uid,
				alias,
				new EntityBinding( joinedEntityDescriptor ),
				joinedEntityDescriptor,
				alias,
				joinType
		);
		getDomainReferenceBinding().injectFromElement( this );
	}

	@Override
	public EntityBinding getDomainReferenceBinding() {
		return (EntityBinding) super.getDomainReferenceBinding();
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
