/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.hql.internal.navigable;

import org.hibernate.sqm.domain.SqmAttribute;
import org.hibernate.sqm.domain.SqmPluralAttribute;
import org.hibernate.sqm.domain.SqmPluralAttributeElement.ElementClassification;
import org.hibernate.sqm.domain.SqmSingularAttribute;
import org.hibernate.sqm.domain.SqmSingularAttribute.SingularAttributeClassification;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.query.from.SqmFromExporter;
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
	protected void validatePathRoot(SqmNavigableBinding binding) {
		// make sure no incoming FromElement comes from a FromElementSpace other
		// than the FromElementSpace joinRhs comes from
		if ( joinRhs.getContainingSpace() != ( (SqmFromExporter) binding ).getExportedFromElement().getContainingSpace() ) {
			throw new SemanticException(
					"Qualified join predicate referred to FromElement [" +
							binding.asLoggableText() + "] outside the FromElementSpace containing the join"
			);
		}
	}

	@Override
	protected void validateIntermediateAttributeJoin(
			SqmNavigableSourceBinding sourceBinding,
			SqmAttribute joinedAttribute) {
		super.validateIntermediateAttributeJoin( sourceBinding, joinedAttribute );

		if ( SqmSingularAttribute.class.isInstance( joinedAttribute ) ) {
			final SqmSingularAttribute attrRef = (SqmSingularAttribute) joinedAttribute;
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
			final SqmPluralAttribute attrRef = (SqmPluralAttribute) joinedAttribute;
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
