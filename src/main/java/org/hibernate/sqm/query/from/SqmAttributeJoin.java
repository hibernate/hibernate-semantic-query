/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.predicate.SqmPredicate;

import org.jboss.logging.Logger;

/**
 * Models a join based on a mapped attribute reference.
 *
 * @author Steve Ebersole
 */
public class SqmAttributeJoin
		extends AbstractJoin
		implements SqmQualifiedJoin, AttributeBinding {
	private static final Logger log = Logger.getLogger( SqmAttributeJoin.class );

	private final Binding lhs;
	private final Attribute joinedAttributeDescriptor;
	private final EntityType intrinsicSubclassIndicator;
	private final boolean fetched;

	private SqmPredicate onClausePredicate;

	public SqmAttributeJoin(
			Binding lhs,
			String uid,
			String alias,
			Attribute joinedAttributeDescriptor,
			EntityType intrinsicSubclassIndicator,
			String sourcePath,
			JoinType joinType,
			boolean fetched) {
		super(
				extractFromElementSpace( lhs ),
				uid,
				alias,
				joinedAttributeDescriptor,
				intrinsicSubclassIndicator,
				sourcePath,
				joinType
		);
		this.lhs = lhs;
		this.joinedAttributeDescriptor = joinedAttributeDescriptor;
		this.intrinsicSubclassIndicator = intrinsicSubclassIndicator;
		this.fetched = fetched;
	}

	private static FromElementSpace extractFromElementSpace(Binding lhs) {
		if ( lhs.getFromElement() == null ) {
			throw new ParsingException( "left-hand-side Binding#getFromElement canno be null" );
		}
		return lhs.getFromElement().getContainingSpace();
	}

	@Override
	public Attribute getBoundAttribute() {
		return getJoinedAttributeDescriptor();
	}

	@Override
	public Binding getLeftHandSide() {
		return lhs;
	}

	@Override
	public void injectFromElementGeneratedForAttribute(SqmAttributeJoin join) {
		throw new ParsingException( "Illegal call to SqmAttributeJoin#injectFromElementGeneratedForAttribute" );
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
