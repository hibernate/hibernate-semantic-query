/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import java.util.Locale;

import org.hibernate.sqm.domain.SqmAttributeReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.SingularSqmAttributeReference;
import org.hibernate.sqm.domain.SingularSqmAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
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
		final SqmAttributeReference attrRef = context().getParsingContext()
				.getConsumerContext()
				.getDomainMetamodel()
				.resolveAttributeReference( lhs.getFromElement().getDomainReferenceBinding().getBoundDomainReference(), pathPart );

		validateIntermediateAttributeJoin( lhs, attrRef );

		return buildAttributeJoin( lhs, attrRef, null );
	}

	protected AttributeBinding buildAttributeJoin(
			DomainReferenceBinding lhs,
			SqmAttributeReference joinedAttributeDescriptor,
			EntityReference subclassIndicator) {
		final AttributeBinding attributeBinding = context().getParsingContext()
				.findOrCreateAttributeBinding( lhs, joinedAttributeDescriptor );

		if ( attributeBinding.getFromElement() == null ) {
			attributeBinding.injectAttributeJoin(
					context().getFromElementBuilder().buildAttributeJoin(
							attributeBinding,
							null,
							subclassIndicator,
							lhs.getFromElement().getPropertyPath().append( joinedAttributeDescriptor.getAttributeName() ),
							getIntermediateJoinType(),
							lhs.getFromElement().getUniqueIdentifier(),
							areIntermediateJoinsFetched(),
							canReuseImplicitJoins()
					)
			);
		}

		return attributeBinding;
	}

	protected void validateIntermediateAttributeJoin(DomainReferenceBinding lhs, SqmAttributeReference joinedAttributeDescriptor) {
		if ( !SingularSqmAttributeReference.class.isInstance( joinedAttributeDescriptor ) ) {
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
			final SingularSqmAttributeReference singularAttribute = (SingularSqmAttributeReference) joinedAttributeDescriptor;
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

	protected SqmAttributeReference resolveAttributeDescriptor(SqmFrom lhs, String attributeName) {
		return resolveAttributeDescriptor( lhs.getDomainReferenceBinding(), attributeName );

	}

	protected SqmAttributeReference resolveAttributeDescriptor(DomainReferenceBinding lhs, String attributeName) {
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
						attributeBinding.getLhs().getFromElement().getPropertyPath().append( attributeBinding.getAttribute().getAttributeName() ),
						JoinType.INNER,
						attributeBinding.getLhs().getFromElement().getUniqueIdentifier(),
						false,
						true
				)
		);
	}

	private boolean joinable(AttributeBinding attributeBinding) {
		if ( attributeBinding.getAttribute() instanceof SingularSqmAttributeReference ) {
			final SingularSqmAttributeReference attrRef = (SingularSqmAttributeReference) attributeBinding.getAttribute();
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
