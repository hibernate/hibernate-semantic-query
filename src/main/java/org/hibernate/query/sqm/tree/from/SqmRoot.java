/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityBinding;

/**
 * @author Steve Ebersole
 */
public class SqmRoot extends AbstractSqmFrom {
	public SqmRoot(
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
	public SqmExpressableTypeEntity getIntrinsicSubclassIndicator() {
		// a root FromElement cannot indicate a subclass intrinsically (as part of its declaration)
		return null;
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
