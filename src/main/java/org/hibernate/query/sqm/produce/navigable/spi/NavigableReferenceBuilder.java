/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.navigable.spi;

import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.domain.PluralAttributeDescriptor;
import org.hibernate.sqm.domain.SingularAttributeDescriptor;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.AttributeReference;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;

/**
 * Contract for building {@link SqmNavigableReference} instance.  Used by
 * Navigable-based {@link SemanticPathPart} interpretation.  Whereas
 * SemanticPathPart interpretation is based on path-parts relation to
 * one another, NavigableReferenceBuilder is context-based (e.g. this
 * handling is different in the FROM clause than in the SELECT clause).
 *
 * @author Steve Ebersole
 */
public class NavigableReferenceBuilder {
	/**
	 * Singleton access
	 */
	public static final NavigableReferenceBuilder INSTANCE = new NavigableReferenceBuilder();

	private NavigableReferenceBuilder() {
	}

	public SqmNavigableReference buildNavigableReference(
			SqmNavigableReference containerReference,
			Navigable navigable,
			boolean isTerminal,
			NavigableReferenceBuilderContext builderContext) {
		final AttributeDescriptor attributeDescriptor = (AttributeDescriptor) navigable;

		final AttributeReference attributeBinding = builderContext.getParsingContext()
				.findOrCreateAttributeBinding( containerReference, attributeDescriptor );

		// join if...
		//		1) not yet
		//		2) either:
		//			a) intermediate join (`isTerminal == false`)
		//			b) and:
		// 				i. attribute is joinable
		// 				ii. `builderContext.forceTerminalJoin() == true`

		if ( attributeBinding.getFromElement() == null ) {
			if ( !isTerminal || (builderContext.forceTerminalJoin() && isJoinable( attributeDescriptor ) ) ) {
				attributeBinding.injectAttributeJoin(
						builderContext.getQuerySpecProcessingState().getFromElementBuilder().buildAttributeJoin(
								attributeBinding,
								isTerminal ? builderContext.getTerminalJoinAlias() : null,
								null,
								containerReference.getFromElement().getPropertyPath().append(
										attributeDescriptor.getAttributeName()
								),
								builderContext.getJoinType(),
								containerReference.getFromElement().getUniqueIdentifier(),
								builderContext.isFetched(),
								builderContext.canReuseJoins()
						)
				);
			}
		}

		return attributeBinding;
	}

	private boolean isJoinable(AttributeDescriptor attribute) {
		if ( SingularAttributeDescriptor.class.isInstance( attribute ) ) {
			final SingularAttributeDescriptor attrRef = (SingularAttributeDescriptor) attribute;
			return attrRef.getAttributeTypeClassification() == SingularAttributeDescriptor.SingularAttributeClassification.EMBEDDED
					|| attrRef.getAttributeTypeClassification() == SingularAttributeDescriptor.SingularAttributeClassification.MANY_TO_ONE
					|| attrRef.getAttributeTypeClassification() == SingularAttributeDescriptor.SingularAttributeClassification.ONE_TO_ONE;
		}
		else {
			// plural attributes can always be joined.
			return true;
		}
	}

	public SqmNavigableReference buildIndexedAccessReference(
			SqmNavigableReference containerReference,
			PluralAttributeDescriptor attribute,
			SqmExpression selector,
			boolean isTerminal,
			NavigableReferenceBuilderContext builderContext) {
		throw new NotYetImplementedException(  );
	}
}
