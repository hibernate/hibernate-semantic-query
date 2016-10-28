/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.domain.PluralAttributeElementReference.ElementClassification;
import org.hibernate.sqm.domain.SingularAttributeReference;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.DomainReferenceBinding;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.from.SqmQualifiedJoin;

/**
 * PathResolver implementation for paths found in a join predicate.
 *
 * @author Steve Ebersole
 */
public class PathResolverJoinPredicateImpl extends PathResolverBasicImpl {
	private final SqmQualifiedJoin joinRhs;

	public PathResolverJoinPredicateImpl(
			ResolutionContext resolutionContext,
			SqmQualifiedJoin joinRhs) {
		super( resolutionContext );
		this.joinRhs = joinRhs;
	}

	@Override
	public boolean canReuseImplicitJoins() {
		return false;
	}

	@Override
	@SuppressWarnings("StatementWithEmptyBody")
	protected void validatePathRoot(DomainReferenceBinding binding) {
		// make sure no incoming FromElement comes from a FromElementSpace other
		// than the FromElementSpace joinRhs comes from
		if ( joinRhs.getContainingSpace() != binding.getFromElement().getContainingSpace() ) {
			throw new SemanticException(
					"Qualified join predicate referred to FromElement [" +
							binding.asLoggableText() + "] outside the FromElementSpace containing the join"
			);
		}
	}

	@Override
	protected void validateIntermediateAttributeJoin(
			DomainReferenceBinding lhs,
			AttributeReference joinedAttribute) {
		super.validateIntermediateAttributeJoin( lhs, joinedAttribute );

		if ( SingularAttributeReference.class.isInstance( joinedAttribute ) ) {
			final SingularAttributeReference attrRef = (SingularAttributeReference) joinedAttribute;
			if ( attrRef.getAttributeTypeClassification() == SingularAttributeClassification.ANY
					|| attrRef.getAttributeTypeClassification() == SingularAttributeClassification.MANY_TO_ONE
					| attrRef.getAttributeTypeClassification() == SingularAttributeClassification.ONE_TO_ONE ) {
				throw new SemanticException(
						"On-clause predicate of a qualified join cannot contain implicit entity joins : " +
								joinedAttribute.getAttributeName()
				);
			}
		}
		else {
			final PluralAttributeReference attrRef = (PluralAttributeReference) joinedAttribute;
			if ( attrRef.getElementReference().getClassification() == ElementClassification.ANY
					|| attrRef.getElementReference().getClassification() == ElementClassification.ONE_TO_MANY
					|| attrRef.getElementReference().getClassification() == ElementClassification.MANY_TO_MANY ) {
				throw new SemanticException(
						"On-clause predicate of a qualified join cannot contain implicit collection joins : " +
								joinedAttribute.getAttributeName()
				);
			}
		}
	}
}
