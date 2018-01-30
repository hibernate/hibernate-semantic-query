/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.navigable.internal;

import org.hibernate.query.sqm.produce.navigable.spi.AbstractNavigableReferenceBuilderContext;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.PluralAttributeDescriptor;
import org.hibernate.sqm.domain.PluralAttributeElementDescriptor;
import org.hibernate.sqm.domain.SingularAttributeDescriptor;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.query.sqm.produce.spi.SemanticQueryBuilder;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;
import org.hibernate.sqm.query.from.SqmQualifiedJoin;

/**
 * @author Steve Ebersole
 */
public class JoinPredicateNavigableReferenceBuilderContextImpl extends AbstractNavigableReferenceBuilderContext {
	private final SqmQualifiedJoin joinRhs;

	public JoinPredicateNavigableReferenceBuilderContextImpl(
			SqmQualifiedJoin joinRhs,
			SemanticQueryBuilder semanticQueryBuilder) {
		super( semanticQueryBuilder );
		this.joinRhs = joinRhs;
	}

	@Override
	public void validatePathRoot(SqmNavigableReference reference) {
		super.validatePathRoot( reference );
		// make sure no incoming FromElement comes from a FromElementSpace other
		// than the FromElementSpace joinRhs comes from
		if ( joinRhs.getContainingSpace() != reference.getFromElement().getContainingSpace() ) {
			throw new SemanticException(
					"Qualified join predicate referred to FromElement [" +
							reference.asLoggableText() + "] outside the FromElementSpace containing the join"
			);
		}

	}

	@Override
	public void validateIntermediateAttributeJoin(SqmNavigableReference lhs, AttributeDescriptor joinedAttribute) {
		if ( SingularAttributeDescriptor.class.isInstance( joinedAttribute ) ) {
			final SingularAttributeDescriptor attrRef = (SingularAttributeDescriptor) joinedAttribute;
			if ( attrRef.getAttributeTypeClassification() == SingularAttributeDescriptor.SingularAttributeClassification.ANY
					|| attrRef.getAttributeTypeClassification() == SingularAttributeDescriptor.SingularAttributeClassification.MANY_TO_ONE
					| attrRef.getAttributeTypeClassification() == SingularAttributeDescriptor.SingularAttributeClassification.ONE_TO_ONE ) {
				throw new SemanticException(
						"On-clause predicate of a qualified join cannot contain implicit entity joins : " +
								joinedAttribute.getAttributeName()
				);
			}
		}
		else {
			final PluralAttributeDescriptor attrRef = (PluralAttributeDescriptor) joinedAttribute;
			if ( attrRef.getElementReference().getClassification() == PluralAttributeElementDescriptor.ElementClassification.ANY
					|| attrRef.getElementReference().getClassification() == PluralAttributeElementDescriptor.ElementClassification.ONE_TO_MANY
					|| attrRef.getElementReference().getClassification() == PluralAttributeElementDescriptor.ElementClassification.MANY_TO_MANY ) {
				throw new SemanticException(
						"On-clause predicate of a qualified join cannot contain implicit collection joins : " +
								joinedAttribute.getAttributeName()
				);
			}
		}
	}
}
