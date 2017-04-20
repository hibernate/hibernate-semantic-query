/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.query.sqm.tree.expression.domain.SqmEntityBinding;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.tree.SqmJoinType;

/**
 * @author Steve Ebersole
 */
public class SqmCrossJoin extends AbstractSqmFrom implements SqmJoin {

	public SqmCrossJoin(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			SqmExpressableTypeEntity entityReference) {
		super(
				fromElementSpace,
				uid,
				alias,
				new SqmEntityBinding( entityReference ),
				entityReference
		);
		getBinding().injectExportedFromElement( this );
	}

	@Override
	public SqmEntityBinding getBinding() {
		return (SqmEntityBinding) super.getBinding();
	}

	public String getEntityName() {
		return getBinding().getBoundNavigable().getEntityName();
	}

	@Override
	public SqmJoinType getJoinType() {
		return SqmJoinType.CROSS;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCrossJoinedFromElement( this );
	}
}
