/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.SqmEntityIdentifier;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmPluralAttributeElement;
import org.hibernate.sqm.domain.SqmPluralAttributeIndex;
import org.hibernate.sqm.domain.SqmPluralAttribute;
import org.hibernate.sqm.domain.SqmSingularAttribute;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.CollectionElementBinding;
import org.hibernate.sqm.query.expression.domain.CollectionElementBindingBasic;
import org.hibernate.sqm.query.expression.domain.CollectionElementBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.CollectionElementBindingEntity;
import org.hibernate.sqm.query.expression.domain.CollectionIndexBinding;
import org.hibernate.sqm.query.expression.domain.CollectionIndexBindingBasic;
import org.hibernate.sqm.query.expression.domain.CollectionIndexBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.CollectionIndexBindingEntity;
import org.hibernate.sqm.query.expression.domain.SqmEntityIdentifierBinding;
import org.hibernate.sqm.query.expression.domain.SqmEntityIdentifierBindingBasic;
import org.hibernate.sqm.query.expression.domain.SqmEntityIdentifierBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.SqmEntityIdentifierEmbedded;
import org.hibernate.sqm.query.expression.domain.SqmEntityTypedBinding;
import org.hibernate.sqm.query.expression.domain.EntityBindingImpl;
import org.hibernate.sqm.query.expression.domain.IndexedElementBindingBasic;
import org.hibernate.sqm.query.expression.domain.IndexedElementBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.IndexedElementBindingEntity;
import org.hibernate.sqm.query.expression.domain.MaxElementBindingBasic;
import org.hibernate.sqm.query.expression.domain.MaxElementBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.MaxElementBindingEntity;
import org.hibernate.sqm.query.expression.domain.MaxIndexBindingBasic;
import org.hibernate.sqm.query.expression.domain.MaxIndexBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.MaxIndexBindingEntity;
import org.hibernate.sqm.query.expression.domain.MinElementBindingBasic;
import org.hibernate.sqm.query.expression.domain.MinElementBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.MinElementBindingEntity;
import org.hibernate.sqm.query.expression.domain.MinIndexBindingBasic;
import org.hibernate.sqm.query.expression.domain.MinIndexBindingEmbeddable;
import org.hibernate.sqm.query.expression.domain.MinIndexBindingEntity;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.query.expression.domain.SqmPluralAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingBasic;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingEmbedded;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingEntity;
import org.hibernate.sqm.query.expression.domain.SqmRestrictedCollectionElementBinding;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmFromExporter;

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

	public static FromElementSpace extractSpace(SqmFromExporter exporter) {
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

	public static CollectionElementBinding createCollectionElementBinding(
			SqmNavigableSourceBinding source,
			SqmPluralAttributeElement elementDescriptor) {
		assert source instanceof SqmPluralAttributeBinding;
		final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) source;

		switch ( elementDescriptor.getClassification() ) {
			case BASIC: {
				return new CollectionElementBindingBasic( pluralAttributeBinding );
			}
			case EMBEDDABLE: {
				return new CollectionElementBindingEmbedded( pluralAttributeBinding );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new CollectionElementBindingEntity( pluralAttributeBinding );
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

	public static CollectionElementBinding createCollectionElementBinding(
			CollectionPartBindingType bindingType,
			SqmNavigableSourceBinding source,
			SqmPluralAttributeElement elementDescriptor) {
		assert source instanceof SqmPluralAttributeBinding;
		final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) source;

		switch ( elementDescriptor.getClassification() ) {
			case BASIC: {
				switch ( bindingType ) {
					case MAX: {
						return new MaxElementBindingBasic( pluralAttributeBinding );
					}
					case MIN: {
						return new MinElementBindingBasic( pluralAttributeBinding );
					}
					default: {
						return new CollectionElementBindingBasic( pluralAttributeBinding );
					}
				}
			}
			case EMBEDDABLE: {
				switch ( bindingType ) {
					case MAX: {
						return new MaxElementBindingEmbedded( pluralAttributeBinding );
					}
					case MIN: {
						return new MinElementBindingEmbedded( pluralAttributeBinding );
					}
					default: {
						return new CollectionElementBindingEmbedded( pluralAttributeBinding );
					}
				}
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				switch ( bindingType ) {
					case MAX: {
						return new MaxElementBindingEntity( pluralAttributeBinding );
					}
					case MIN: {
						return new MinElementBindingEntity( pluralAttributeBinding );
					}
					default: {
						return new CollectionElementBindingEntity( pluralAttributeBinding );
					}
				}
			}
			default: {
				throw new NotYetImplementedException();
			}
		}
	}

	public static CollectionIndexBinding createCollectionIndexBinding(
			SqmNavigableSourceBinding source,
			SqmPluralAttributeIndex indexDescriptor) {
		assert source instanceof SqmPluralAttributeBinding;
		final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) source;

		switch ( indexDescriptor.getClassification() ) {
			case BASIC: {
				return new CollectionIndexBindingBasic( pluralAttributeBinding );
			}
			case EMBEDDABLE: {
				return new CollectionIndexBindingEmbedded( pluralAttributeBinding );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new CollectionIndexBindingEntity( pluralAttributeBinding );
			}
			default: {
				throw new NotYetImplementedException(  );
			}
		}
	}


	public static CollectionIndexBinding createCollectionIndexBinding(
			CollectionPartBindingType bindingType,
			SqmNavigableSourceBinding source,
			SqmPluralAttributeIndex indexDescriptor) {
		assert source instanceof SqmPluralAttributeBinding;
		final SqmPluralAttributeBinding pluralAttributeBinding = (SqmPluralAttributeBinding) source;

		switch ( indexDescriptor.getClassification() ) {
			case BASIC: {
				switch ( bindingType ) {
					case MAX: {
						return new MaxIndexBindingBasic( pluralAttributeBinding );
					}
					case MIN: {
						return new MinIndexBindingBasic( pluralAttributeBinding );
					}
					default: {
						return new CollectionIndexBindingBasic( pluralAttributeBinding );
					}
				}
			}
			case EMBEDDABLE: {
				switch ( bindingType ) {
					case MAX: {
						return new MaxIndexBindingEmbedded( pluralAttributeBinding );
					}
					case MIN: {
						return new MinIndexBindingEmbeddable( pluralAttributeBinding );
					}
					default: {
						return new CollectionIndexBindingEmbedded( pluralAttributeBinding );
					}
				}
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				switch ( bindingType ) {
					case MAX: {
						return new MaxIndexBindingEntity( pluralAttributeBinding );
					}
					case MIN: {
						return new MinIndexBindingEntity( pluralAttributeBinding );
					}
					default: {
						return new CollectionIndexBindingEntity( pluralAttributeBinding );
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
				return new IndexedElementBindingBasic( pluralAttributeBinding, selectorExpression );
			}
			case EMBEDDABLE: {
				return new IndexedElementBindingEmbedded( pluralAttributeBinding, selectorExpression );
			}
			case ONE_TO_MANY:
			case MANY_TO_MANY: {
				return new IndexedElementBindingEntity( pluralAttributeBinding, selectorExpression );
			}
			default: {
				throw new NotYetImplementedException();
			}
		}
	}

	public static SqmEntityTypedBinding createEntityBinding(SqmExpressableTypeEntity entityReference) {
		return new EntityBindingImpl( entityReference );
	}

	private NavigableBindingHelper() {
	}
}
