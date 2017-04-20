/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.internal;

import org.hibernate.query.sqm.NotYetImplementedException;
import org.hibernate.query.sqm.domain.SqmEntityIdentifier;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.domain.SqmPluralAttributeElement;
import org.hibernate.query.sqm.domain.SqmPluralAttributeIndex;
import org.hibernate.query.sqm.domain.SqmPluralAttribute;
import org.hibernate.query.sqm.domain.SqmSingularAttribute;
import org.hibernate.query.sqm.domain.SqmNavigable;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.query.sqm.ParsingException;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionElementBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionElementBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionElementBindingEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionElementBindingEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexBindingEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexBindingEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityIdentifierBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityIdentifierBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityIdentifierBindingEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityIdentifierEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityTypedBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmIndexedElementBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmIndexedElementBindingEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmIndexedElementBindingEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmMaxElementBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmMaxElementBindingEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmMaxElementBindingEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmMaxIndexBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmMaxIndexBindingEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmMaxIndexBindingEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmMinElementBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmMinElementBindingEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmMinElementBindingEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmMinIndexBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmMinIndexBindingEmbeddable;
import org.hibernate.query.sqm.tree.expression.domain.SqmMinIndexBindingEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeBindingEmbedded;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeBindingEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmRestrictedCollectionElementBinding;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.query.sqm.tree.from.SqmFromExporter;

/**
 * @author Steve Ebersole
 */
public class NavigableBindingHelper {
	public static SqmFrom resolveExportedFromElement(SqmNavigableBinding binding) {
		if ( binding instanceof SqmFromExporter ) {
			return ( (SqmFromExporter) binding ).getExportedFromElement();
		}

		if ( binding.getSourceBinding() != null ) {
			return resolveExportedFromElement( binding.getSourceBinding() );
		}

		throw new ParsingException( "Could not resolve SqmFrom element from NavigableBinding : " + binding );
	}

	public static SqmFromElementSpace extractSpace(SqmFromExporter exporter) {
		return exporter.getExportedFromElement() == null ? null : exporter.getExportedFromElement() .getContainingSpace();
	}

	public static SqmNavigableBinding createNavigableBinding(SqmNavigableSourceBinding source, SqmNavigable sqmNavigable) {
		if ( sqmNavigable instanceof SqmPluralAttribute ) {
			return createPluralAttributeBinding( source, (SqmPluralAttribute) sqmNavigable );
		}
		else if ( sqmNavigable instanceof SqmSingularAttribute ) {
			return createSingularAttributeBinding( source, (SqmSingularAttribute) sqmNavigable );
		}
		else if ( sqmNavigable instanceof SqmPluralAttributeElement ) {
			return createCollectionElementBinding( source, (SqmPluralAttributeElement) sqmNavigable );
		}
		else if ( sqmNavigable instanceof SqmPluralAttributeIndex ) {
			return createCollectionIndexBinding( source, (SqmPluralAttributeIndex) sqmNavigable );
		}
		else if ( sqmNavigable instanceof SqmExpressableTypeEntity ) {
			// for anything else source should be null
			assert source == null;
			return createEntityBinding( (SqmExpressableTypeEntity) sqmNavigable );
		}
		else if ( sqmNavigable instanceof SqmEntityIdentifier ) {
			assert source instanceof SqmEntityTypedBinding;
			return createEntityIdentiferBinding( (SqmEntityTypedBinding) source, (SqmEntityIdentifier) sqmNavigable );
		}
		throw new ParsingException( "Unexpected SqmNavigable for creation of NavigableBinding : " + sqmNavigable );
	}

	private static SqmEntityIdentifierBinding createEntityIdentiferBinding(
			SqmEntityTypedBinding sourceBinding,
			SqmEntityIdentifier sqmNavigable) {
		if ( sqmNavigable.getExportedDomainType() instanceof SqmDomainTypeBasic ) {
			return new SqmEntityIdentifierBindingBasic( sourceBinding, sqmNavigable );
		}
		else {
			return new SqmEntityIdentifierBindingEmbedded( sourceBinding, (SqmEntityIdentifierEmbedded) sqmNavigable );
		}
	}

	private static SqmPluralAttributeBinding createPluralAttributeBinding(
			SqmNavigableSourceBinding lhs,
			SqmPluralAttribute pluralSqmAttribute) {
		return new SqmPluralAttributeBinding( lhs, pluralSqmAttribute );
	}

	public static SqmSingularAttributeBinding createSingularAttributeBinding(SqmNavigableSourceBinding sourceBinding, SqmSingularAttribute attribute) {
		switch ( attribute.getAttributeTypeClassification() ) {
			case BASIC: {
				return new SqmSingularAttributeBindingBasic( sourceBinding, attribute );
			}
			case EMBEDDED: {
				return new SqmSingularAttributeBindingEmbedded( sourceBinding, attribute );
			}
			case ONE_TO_ONE:
			case MANY_TO_ONE: {
				return new SqmSingularAttributeBindingEntity( sourceBinding, attribute );
			}
			default: {
				throw new NotYetImplementedException();
			}
		}
	}

	public static SqmCollectionElementBinding createCollectionElementBinding(
			SqmNavigableSourceBinding source,
			SqmPluralAttributeElement elementDescriptor) {
		assert source instanceof SqmPluralAttributeBinding;
		final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) source;

		switch ( elementDescriptor.getClassification() ) {
			case BASIC: {
				return new SqmCollectionElementBindingBasic( pluralAttributeBinding );
			}
			case EMBEDDABLE: {
				return new SqmCollectionElementBindingEmbedded( pluralAttributeBinding );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new SqmCollectionElementBindingEntity( pluralAttributeBinding );
			}
			default: {
				throw new NotYetImplementedException();
			}
		}
	}

	enum CollectionPartBindingType {
		NORMAL,
		MIN,
		MAX
	}

	public static SqmCollectionElementBinding createCollectionElementBinding(
			CollectionPartBindingType bindingType,
			SqmNavigableSourceBinding source,
			SqmPluralAttributeElement elementDescriptor) {
		assert source instanceof SqmPluralAttributeBinding;
		final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) source;

		switch ( elementDescriptor.getClassification() ) {
			case BASIC: {
				switch ( bindingType ) {
					case MAX: {
						return new SqmMaxElementBindingBasic( pluralAttributeBinding );
					}
					case MIN: {
						return new SqmMinElementBindingBasic( pluralAttributeBinding );
					}
					default: {
						return new SqmCollectionElementBindingBasic( pluralAttributeBinding );
					}
				}
			}
			case EMBEDDABLE: {
				switch ( bindingType ) {
					case MAX: {
						return new SqmMaxElementBindingEmbedded( pluralAttributeBinding );
					}
					case MIN: {
						return new SqmMinElementBindingEmbedded( pluralAttributeBinding );
					}
					default: {
						return new SqmCollectionElementBindingEmbedded( pluralAttributeBinding );
					}
				}
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				switch ( bindingType ) {
					case MAX: {
						return new SqmMaxElementBindingEntity( pluralAttributeBinding );
					}
					case MIN: {
						return new SqmMinElementBindingEntity( pluralAttributeBinding );
					}
					default: {
						return new SqmCollectionElementBindingEntity( pluralAttributeBinding );
					}
				}
			}
			default: {
				throw new NotYetImplementedException();
			}
		}
	}

	public static SqmCollectionIndexBinding createCollectionIndexBinding(
			SqmNavigableSourceBinding source,
			SqmPluralAttributeIndex indexDescriptor) {
		assert source instanceof SqmPluralAttributeBinding;
		final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) source;

		switch ( indexDescriptor.getClassification() ) {
			case BASIC: {
				return new SqmCollectionIndexBindingBasic( pluralAttributeBinding );
			}
			case EMBEDDABLE: {
				return new SqmCollectionIndexBindingEmbedded( pluralAttributeBinding );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new SqmCollectionIndexBindingEntity( pluralAttributeBinding );
			}
			default: {
				throw new NotYetImplementedException(  );
			}
		}
	}


	public static SqmCollectionIndexBinding createCollectionIndexBinding(
			CollectionPartBindingType bindingType,
			SqmNavigableSourceBinding source,
			SqmPluralAttributeIndex indexDescriptor) {
		assert source instanceof SqmPluralAttributeBinding;
		final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) source;

		switch ( indexDescriptor.getClassification() ) {
			case BASIC: {
				switch ( bindingType ) {
					case MAX: {
						return new SqmMaxIndexBindingBasic( pluralAttributeBinding );
					}
					case MIN: {
						return new SqmMinIndexBindingBasic( pluralAttributeBinding );
					}
					default: {
						return new SqmCollectionIndexBindingBasic( pluralAttributeBinding );
					}
				}
			}
			case EMBEDDABLE: {
				switch ( bindingType ) {
					case MAX: {
						return new SqmMaxIndexBindingEmbedded( pluralAttributeBinding );
					}
					case MIN: {
						return new SqmMinIndexBindingEmbeddable( pluralAttributeBinding );
					}
					default: {
						return new SqmCollectionIndexBindingEmbedded( pluralAttributeBinding );
					}
				}
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				switch ( bindingType ) {
					case MAX: {
						return new SqmMaxIndexBindingEntity( pluralAttributeBinding );
					}
					case MIN: {
						return new SqmMinIndexBindingEntity( pluralAttributeBinding );
					}
					default: {
						return new SqmCollectionIndexBindingEntity( pluralAttributeBinding );
					}
				}
			}
			default: {
				throw new NotYetImplementedException(  );
			}
		}
	}

	public static SqmRestrictedCollectionElementBinding createIndexedCollectionElementBinding(
			SqmPluralAttributeBinding pluralAttributeBinding,
			SqmPluralAttributeElement elementDescriptor,
			SqmExpression selectorExpression) {

		switch ( elementDescriptor.getClassification() ) {
			case BASIC: {
				return new SqmIndexedElementBindingBasic( pluralAttributeBinding, selectorExpression );
			}
			case EMBEDDABLE: {
				return new SqmIndexedElementBindingEmbedded( pluralAttributeBinding, selectorExpression );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new SqmIndexedElementBindingEntity( pluralAttributeBinding, selectorExpression );
			}
			default: {
				throw new NotYetImplementedException();
			}
		}
	}

	public static SqmEntityTypedBinding createEntityBinding(SqmExpressableTypeEntity entityReference) {
		return new SqmEntityBinding( entityReference );
	}

	private NavigableBindingHelper() {
	}
}
