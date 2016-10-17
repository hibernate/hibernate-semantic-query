/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import java.util.Locale;

import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.domain.SingularAttributeReference;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.AttributeBinding;
import org.hibernate.sqm.parser.common.DomainReferenceBinding;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * Template support for PathResolver implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPathResolverImpl implements PathResolver {
	private final ResolutionContext context;

	public AbstractPathResolverImpl(ResolutionContext context) {
		this.context = context;
	}

	protected ResolutionContext context() {
		return context;
	}

	protected DomainReferenceBinding resolveAnyIntermediateAttributePathJoins(
			DomainReferenceBinding lhs,
			String[] pathParts) {
		// build joins for any intermediate path parts
		for ( int i = 0, max = pathParts.length-1; i < max; i++ ) {
			lhs = buildIntermediateAttributeJoin( lhs, pathParts[i] );
		}
		return lhs;
	}

	protected AttributeBinding buildIntermediateAttributeJoin(
			DomainReferenceBinding lhs,
			String pathPart) {
		final AttributeReference attrRef = context().getParsingContext()
				.getConsumerContext()
				.getDomainMetamodel()
				.resolveAttributeReference( lhs.getFromElement().getDomainReferenceBinding().getBoundDomainReference(), pathPart );

		validateIntermediateAttributeJoin( lhs, attrRef );

		return buildAttributeJoin( lhs, attrRef, null );
	}

	protected AttributeBinding buildAttributeJoin(
			DomainReferenceBinding lhs,
			AttributeReference joinedAttributeDescriptor,
			EntityReference subclassIndicator) {
		final AttributeBinding attributeBinding = context().getParsingContext()
				.findOrCreateAttributeBinding( lhs, joinedAttributeDescriptor );

		if ( attributeBinding.getFromElement() == null ) {
			attributeBinding.injectAttributeJoin(
					context().getFromElementBuilder().buildAttributeJoin(
							attributeBinding,
							null,
							subclassIndicator,
							lhs.getFromElement().asLoggableText() + '.' + joinedAttributeDescriptor.getAttributeName(),
							getIntermediateJoinType(),
							areIntermediateJoinsFetched(),
							canReuseImplicitJoins()
					)
			);
		}

		return attributeBinding;
	}

	protected void validateIntermediateAttributeJoin(DomainReferenceBinding lhs, AttributeReference joinedAttributeDescriptor) {
		if ( !SingularAttributeReference.class.isInstance( joinedAttributeDescriptor ) ) {
			throw new SemanticException(
					String.format(
							Locale.ROOT,
							"Attribute [%s -> %s] is plural, cannot be used as non-terminal in path expression",
							lhs.getFromElement().asLoggableText(),
							joinedAttributeDescriptor.getAttributeName()
					)
			);
		}
		else {
			// make sure it is Bindable
			final SingularAttributeReference singularAttribute = (SingularAttributeReference) joinedAttributeDescriptor;
			if ( !canBeDereferenced( singularAttribute.getAttributeTypeClassification() ) ) {
				throw new SemanticException(
						String.format(
								Locale.ROOT,
								"SingularAttribute [%s -> %s] reports is cannot be de-referenced, therefore cannot be used as non-terminal in path expression",
								lhs.getFromElement().asLoggableText(),
								joinedAttributeDescriptor.getAttributeName()
						)
				);
			}
		}
	}

	private boolean canBeDereferenced(SingularAttributeClassification classification) {
		return classification == SingularAttributeClassification.EMBEDDED
				|| classification == SingularAttributeClassification.MANY_TO_ONE
				|| classification == SingularAttributeClassification.ONE_TO_ONE;
	}

	protected JoinType getIntermediateJoinType() {
		return JoinType.LEFT;
	}

	protected boolean areIntermediateJoinsFetched() {
		return false;
	}

	protected AttributeReference resolveAttributeDescriptor(SqmFrom lhs, String attributeName) {
		return resolveAttributeDescriptor( lhs.getDomainReferenceBinding(), attributeName );

	}

	protected AttributeReference resolveAttributeDescriptor(DomainReferenceBinding lhs, String attributeName) {
		return context().getParsingContext()
				.getConsumerContext()
				.getDomainMetamodel()
				.resolveAttributeReference( lhs.getBoundDomainReference(), attributeName );
	}

	protected void resolveAttributeJoinIfNot(AttributeBinding attributeBinding) {
		if ( attributeBinding.getFromElement() != null ) {
			return;
		}

		if ( !joinable( attributeBinding ) ) {
			return;
		}

		attributeBinding.injectAttributeJoin(
				context().getFromElementBuilder().buildAttributeJoin(
						attributeBinding,
						null,
						null,
						attributeBinding.getLhs().getFromElement().asLoggableText() + '.' + attributeBinding.getAttribute().getAttributeName(),
						JoinType.INNER,
						false,
						true
				)
		);
	}

	private boolean joinable(AttributeBinding attributeBinding) {
		if ( attributeBinding.getAttribute() instanceof SingularAttributeReference ) {
			final SingularAttributeReference attrRef = (SingularAttributeReference) attributeBinding.getAttribute();
			if ( attrRef.getAttributeTypeClassification() == SingularAttributeClassification.BASIC
					|| attrRef.getAttributeTypeClassification() == SingularAttributeClassification.ANY ) {
				return false;
			}
			return true;
		}

		// Plural attributes are always joinable
		return true;
	}
}
