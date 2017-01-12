/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.query.SqmJoinType;
import org.hibernate.sqm.query.expression.domain.SqmEntityBinding;
import org.hibernate.sqm.query.predicate.SqmPredicate;

/**
 * @author Steve Ebersole
 */
public class SqmEntityJoin
		extends AbstractSqmJoin
		implements SqmQualifiedJoin {
	private SqmPredicate onClausePredicate;

	public SqmEntityJoin(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			SqmExpressableTypeEntity joinedEntityDescriptor,
			SqmJoinType joinType) {
		super(
				fromElementSpace,
				uid,
				alias,
				new SqmEntityBinding( joinedEntityDescriptor ),
				joinedEntityDescriptor,
				joinType
		);
		getEntityBinding().injectExportedFromElement( this );
	}

	@Override
	public SqmEntityBinding getBinding() {
		return getEntityBinding();
	}

	public SqmEntityBinding getEntityBinding() {
		return (SqmEntityBinding) super.getBinding();
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
