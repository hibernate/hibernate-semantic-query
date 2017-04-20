/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityBinding;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

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
