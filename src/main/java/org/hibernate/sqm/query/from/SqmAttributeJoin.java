/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.domain.AttributeReference;
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

	private final AttributeReference attributeBinding;
	private final EntityDescriptor intrinsicSubclassIndicator;
	private final String lhsUniqueIdentifier;
	private final boolean fetched;

	private SqmPredicate onClausePredicate;

	public SqmAttributeJoin(
			FromElementSpace containingSpace,
			AttributeReference attributeBinding,
			String uid,
			String alias,
			EntityDescriptor intrinsicSubclassIndicator,
			PropertyPath sourcePath,
			JoinType joinType,
			String lhsUniqueIdentifier,
			boolean fetched) {
		super(
				containingSpace,
				uid,
				alias,
				attributeBinding,
				intrinsicSubclassIndicator,
				sourcePath,
				joinType
		);

		// todo : this shows we ought to drop `lhsUniqueIdentifier`
		assert lhsUniqueIdentifier.equals( attributeBinding.getLhs().getFromElement().getUniqueIdentifier() ) :
				"table uids did not match : " + lhsUniqueIdentifier + " <=> " + attributeBinding.getLhs().getFromElement().getUniqueIdentifier();

		this.attributeBinding = attributeBinding;
		this.intrinsicSubclassIndicator = intrinsicSubclassIndicator;
		this.lhsUniqueIdentifier = lhsUniqueIdentifier;
		this.fetched = fetched;

		attributeBinding.injectAttributeJoin( this );
	}

	public AttributeReference getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public AttributeReference getDomainReferenceBinding() {
		return getAttributeBinding();
	}

	@Override
	public EntityDescriptor getIntrinsicSubclassIndicator() {
		return intrinsicSubclassIndicator;
	}

	public String getLhsUniqueIdentifier() {
		return lhsUniqueIdentifier;
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
	public Navigable getExpressionType() {
		return attributeBinding.getAttribute();
	}

	@Override
	public Navigable getInferableType() {
		return attributeBinding.getAttribute();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitQualifiedAttributeJoinFromElement( this );
	}
}
