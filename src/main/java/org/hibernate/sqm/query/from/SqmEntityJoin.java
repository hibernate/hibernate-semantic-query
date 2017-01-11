/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.expression.domain.EntityBindingImpl;
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
			SqmExpressableTypeEntity joinedEntityDescriptor,
			JoinType joinType) {
		super(
				fromElementSpace,
				uid,
				alias,
				new EntityBindingImpl( joinedEntityDescriptor ),
				joinedEntityDescriptor,
				joinType
		);
		getEntityBinding().injectExportedFromElement( this );
	}

	@Override
	public EntityBindingImpl getBinding() {
		return getEntityBinding();
	}

	public EntityBindingImpl getEntityBinding() {
		return (EntityBindingImpl) super.getBinding();
	}

	public String getEntityName() {
		return getEntityBinding().getBoundNavigable().getEntityName();
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
