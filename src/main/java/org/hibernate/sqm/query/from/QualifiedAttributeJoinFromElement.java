/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.predicate.Predicate;

import org.jboss.logging.Logger;

/**
 * Models a join based on a mapped attribute reference.
 *
 * @author Steve Ebersole
 */
public class QualifiedAttributeJoinFromElement
		extends AbstractJoinedFromElement
		implements QualifiedJoinedFromElement, AttributeBinding {
	private static final Logger log = Logger.getLogger( QualifiedAttributeJoinFromElement.class );

	private final AttributeBindingSource lhs;
	private final Attribute joinedAttributeDescriptor;
	private final EntityType intrinsicSubclassIndicator;
	private final boolean fetched;

	private Predicate onClausePredicate;

	public QualifiedAttributeJoinFromElement(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			Attribute joinedAttributeDescriptor,
			EntityType intrinsicSubclassIndicator,
			String sourcePath,
			JoinType joinType,
			AttributeBindingSource lhs,
			boolean fetched) {
		super(
				fromElementSpace,
				uid,
				alias,
				(Bindable) joinedAttributeDescriptor,
				intrinsicSubclassIndicator,
				sourcePath,
				joinType
		);
		this.lhs = lhs;
		this.joinedAttributeDescriptor = joinedAttributeDescriptor;
		this.intrinsicSubclassIndicator = intrinsicSubclassIndicator;
		this.fetched = fetched;
	}

	/**
	 * The FromElement alias for the "left hand side" of this join.
	 *
	 * @return The LHS FromElement alias.
	 */
	public String getLhsAlias() {
		return lhs.getFromElement().getIdentificationVariable();
	}

	@Override
	public Attribute getBoundAttribute() {
		return getJoinedAttributeDescriptor();
	}

	@Override
	public AttributeBindingSource getAttributeBindingSource() {
		return lhs;
	}

	/**
	 * Obtain the descriptor for the attribute defining the join.
	 *
	 * @return The attribute descriptor
	 */
	public Attribute getJoinedAttributeDescriptor() {
		return joinedAttributeDescriptor;
	}

	@Override
	public EntityType getIntrinsicSubclassIndicator() {
		return intrinsicSubclassIndicator;
	}

	public boolean isFetched() {
		return fetched;
	}

	@Override
	public Attribute resolveAttribute(String attributeName) {
		if ( getJoinedAttributeDescriptor() instanceof SingularAttribute ) {
			final SingularAttribute singularAttribute = (SingularAttribute) getJoinedAttributeDescriptor();
			if ( !ManagedType.class.isInstance( singularAttribute.getBoundType() ) ) {
				throw new AttributeResolutionException(
						"Cannot resolve Attribute [" + attributeName + "] from non-ManagedType [" + singularAttribute.getBoundType() + "]"
				);
			}
			return ( (ManagedType) singularAttribute.getBoundType() ).findAttribute( attributeName );
		}
		else if ( getJoinedAttributeDescriptor() instanceof PluralAttribute ) {
			// Use the element type...
			final PluralAttribute pluralAttribute = (PluralAttribute) getJoinedAttributeDescriptor();
			if ( !ManagedType.class.isInstance( pluralAttribute.getElementType() ) ) {
				throw new AttributeResolutionException(
						"Cannot resolve Attribute [" + attributeName + "] from non-ManagedType [" + pluralAttribute.getElementType() + "]"
				);
			}
			return ( (ManagedType) pluralAttribute.getElementType() ).findAttribute( attributeName );
		}

		throw new AttributeResolutionException(
				"Unexpected Attribute Type [" + getJoinedAttributeDescriptor() + "] as left-hand side for attribute reference [" +
						attributeName + "]"
		);
	}

	@Override
	public Predicate getOnClausePredicate() {
		return onClausePredicate;
	}

	public void setOnClausePredicate(Predicate predicate) {
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
