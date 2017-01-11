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
import org.hibernate.sqm.query.expression.domain.SqmAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.predicate.SqmPredicate;

import org.jboss.logging.Logger;

/**
 * Models a join based on a mapped attribute reference.
 *
 * @author Steve Ebersole
 */
public class SqmAttributeJoin
		extends AbstractJoin
		implements SqmQualifiedJoin {
	private static final Logger log = Logger.getLogger( SqmAttributeJoin.class );

	private final SqmFrom lhs;
	private final SqmAttributeBinding attributeBinding;
	private final SqmExpressableTypeEntity intrinsicSubclassIndicator;
	private final boolean fetched;

	private SqmPredicate onClausePredicate;

	public SqmAttributeJoin(
			SqmFrom lhs,
			SqmAttributeBinding attributeBinding,
			String uid,
			String alias,
			SqmExpressableTypeEntity intrinsicSubclassIndicator,
			JoinType joinType,
			boolean fetched) {
		super(
				attributeBinding.getSourceBinding().getExportedFromElement().getContainingSpace(),
				uid,
				alias,
				attributeBinding,
				intrinsicSubclassIndicator,
				joinType
		);
		this.lhs = lhs;

		this.attributeBinding = attributeBinding;
		this.intrinsicSubclassIndicator = intrinsicSubclassIndicator;
		this.fetched = fetched;

		attributeBinding.injectExportedFromElement( this );
	}

	public SqmFrom getLhs() {
		return lhs;
	}

	public SqmAttributeBinding getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public SqmNavigableBinding getBinding() {
		return getAttributeBinding();
	}

	@Override
	public SqmExpressableTypeEntity getIntrinsicSubclassIndicator() {
		return intrinsicSubclassIndicator;
	}

	public boolean isFetched() {
		return fetched;
	}

	@Override
	public SqmPredicate getOnClausePredicate() {
		return onClausePredicate;
	}

	public void setOnClausePredicate(SqmPredicate predicate) {
		log.tracef(
				"Setting join predicate [%s] (was [%s])",
				predicate.toString(),
				this.onClausePredicate == null ? "<null>" : this.onClausePredicate.toString()
		);

		this.onClausePredicate = predicate;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitQualifiedAttributeJoinFromElement( this );
	}
}
