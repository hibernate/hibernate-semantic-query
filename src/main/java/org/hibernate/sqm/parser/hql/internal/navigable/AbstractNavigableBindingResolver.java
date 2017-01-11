/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.hql.internal.navigable;

import java.util.Locale;

import org.hibernate.sqm.domain.SqmAttribute;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.SqmSingularAttribute;
import org.hibernate.sqm.domain.SqmSingularAttribute.SingularAttributeClassification;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.expression.domain.SqmAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;

/**
 * Template support for PathResolver implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractNavigableBindingResolver implements NavigableBindingResolver {
	private final ResolutionContext context;

	public AbstractNavigableBindingResolver(ResolutionContext context) {
		this.context = context;
	}

	protected ResolutionContext context() {
		return context;
	}

	protected SqmNavigableSourceBinding resolveAnyIntermediateAttributePathJoins(
			SqmNavigableSourceBinding sourceBinding,
			String[] pathParts) {
		// build joins for any intermediate path parts
		for ( int i = 0, max = pathParts.length-1; i < max; i++ ) {
			sourceBinding = buildIntermediateAttributeJoin( sourceBinding, pathParts[i] );
		}
		return sourceBinding;
	}

	protected SqmNavigableSourceBinding buildIntermediateAttributeJoin(
			SqmNavigableSourceBinding sourceBinding,
			String navigableName) {
		final SqmAttribute attrRef = (SqmAttribute) context().getParsingContext()
				.getConsumerContext()
				.getDomainMetamodel()
				.resolveNavigable( sourceBinding.getBoundNavigable(), navigableName );

		validateIntermediateAttributeJoin( sourceBinding, attrRef );

		return (SqmNavigableSourceBinding) buildAttributeJoin( sourceBinding, attrRef, null );
	}

	protected SqmNavigableBinding buildAttributeJoin(
			SqmNavigableSourceBinding sourceBinding,
			SqmNavigable joinedNavigable,
			SqmExpressableTypeEntity subclassIndicator) {
		final SqmAttributeBinding attributeBinding = (SqmAttributeBinding) context().getParsingContext()
				.findOrCreateNavigableBinding( sourceBinding, joinedNavigable );

		if ( attributeBinding.getExportedFromElement() == null ) {
			attributeBinding.injectExportedFromElement(
					context().getFromElementBuilder().buildAttributeJoin(
							attributeBinding,
							null,
							subclassIndicator,
							getIntermediateJoinType(),
							areIntermediateJoinsFetched(),
							canReuseImplicitJoins()
					)
			);
		}

		return attributeBinding;
	}

	protected void validateIntermediateAttributeJoin(
			SqmNavigableSourceBinding sourceBinding,
			SqmAttribute joinedAttributeDescriptor) {
		if ( !SqmSingularAttribute.class.isInstance( joinedAttributeDescriptor ) ) {
			throw new SemanticException(
					String.format(
							Locale.ROOT,
							"Attribute [%s -> %s] is plural, cannot be used as non-terminal in path expression",
							sourceBinding.asLoggableText(),
							joinedAttributeDescriptor.getAttributeName()
					)
			);
		}
		else {
			// make sure it is Bindable
			final SqmSingularAttribute singularAttribute = (SqmSingularAttribute) joinedAttributeDescriptor;
			if ( !canBeDereferenced( singularAttribute.getAttributeTypeClassification() ) ) {
				throw new SemanticException(
						String.format(
								Locale.ROOT,
								"SingularAttribute [%s -> %s] reports is cannot be de-referenced, therefore cannot be used as non-terminal in path expression",
								sourceBinding.asLoggableText(),
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

	protected SqmNavigable resolveNavigable(SqmNavigableSourceBinding sourceBinding, String navigableName) {
		return context().getParsingContext()
				.getConsumerContext()
				.getDomainMetamodel()
				.resolveNavigable( sourceBinding.getBoundNavigable(), navigableName );
	}

	protected void resolveAttributeJoinIfNot(SqmNavigableBinding navigableBinding) {
		if ( !SqmAttributeBinding.class.isInstance( navigableBinding ) ) {
			return;
		}

		SqmAttributeBinding attributeBinding = (SqmAttributeBinding) navigableBinding;
		if ( attributeBinding.getExportedFromElement() != null ) {
			return;
		}

		if ( !joinable( attributeBinding ) ) {
			return;
		}

		attributeBinding.injectExportedFromElement(
				context().getFromElementBuilder().buildAttributeJoin(
						attributeBinding,
						null,
						null,
						JoinType.INNER,
						false,
						true
				)
		);
	}

	private boolean joinable(SqmAttributeBinding attributeBinding) {
		if ( attributeBinding.getBoundNavigable() instanceof SqmSingularAttribute ) {
			final SqmSingularAttribute attrRef = (SqmSingularAttribute) attributeBinding.getBoundNavigable();
			return attrRef.getAttributeTypeClassification() != SingularAttributeClassification.BASIC
					&& attrRef.getAttributeTypeClassification() != SingularAttributeClassification.ANY;
		}

		// Plural attributes are always joinable
		return true;
	}
}
