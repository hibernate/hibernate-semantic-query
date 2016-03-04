/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.path;

import java.util.Locale;

import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.QualifiedJoinedFromElement;

/**
 * PathResolver implementation for paths found in a join predicate.
 *
 * @author Steve Ebersole
 */
public class PathResolverJoinPredicateImpl extends PathResolverBasicImpl {
	private final QualifiedJoinedFromElement joinRhs;

	public PathResolverJoinPredicateImpl(
			ResolutionContext resolutionContext,
			QualifiedJoinedFromElement joinRhs) {
		super( resolutionContext );
		this.joinRhs = joinRhs;
	}

	@Override
	@SuppressWarnings("StatementWithEmptyBody")
	protected void validatePathRoot(FromElement fromElement) {
		// make sure no incoming FromElement comes from a FromElementSpace other
		// than the FromElementSpace joinRhs comes from
		if ( joinRhs.getContainingSpace() != fromElement.getContainingSpace() ) {
			throw new SemanticException(
					"Qualified join predicate referred to FromElement [" +
							fromElement.asLoggableText() + "] outside the FromElementSpace containing the join"
			);
		}
	}

	@Override
	protected void validateIntermediateAttributeJoin(
			AttributeBindingSource lhs,
			Attribute joinedAttribute) {
		if ( joinedAttribute instanceof SingularAttribute ) {
			final SingularAttribute singularAttribute = (SingularAttribute) joinedAttribute;
			if ( singularAttribute.getAttributeTypeClassification() == SingularAttribute.Classification.ANY
					|| singularAttribute.getAttributeTypeClassification() == SingularAttribute.Classification.MANY_TO_ONE
					| singularAttribute.getAttributeTypeClassification() == SingularAttribute.Classification.ONE_TO_ONE ) {
				throw new SemanticException(
						"On-clause predicate of a qualified join cannot contain implicit entity joins : " +
								joinedAttribute.getName()
				);
			}
		}
		else {
			final PluralAttribute pluralAttribute = (PluralAttribute) joinedAttribute;
			if ( pluralAttribute.getElementClassification() == PluralAttribute.ElementClassification.ANY
					|| pluralAttribute.getElementClassification() == PluralAttribute.ElementClassification.ONE_TO_MANY
					|| pluralAttribute.getElementClassification() == PluralAttribute.ElementClassification.MANY_TO_MANY ) {
				throw new SemanticException(
						"On-clause predicate of a qualified join cannot contain implicit collection joins : " +
								joinedAttribute.getName()
				);
			}
		}
	}
}
