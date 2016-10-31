/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.SingularAttributeReference;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.from.Downcast;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class PathResolverBasicImpl extends AbstractPathResolverImpl {
	private static final Logger log = Logger.getLogger( PathResolverBasicImpl.class );

	public PathResolverBasicImpl(ResolutionContext context) {
		super( context );
	}

	protected boolean shouldRenderTerminalAttributeBindingAsJoin() {
		return false;
	}

	@Override
	public boolean canReuseImplicitJoins() {
		return true;
	}

	@Override
	public DomainReferenceBinding resolvePath(String... pathParts) {
		return resolveTreatedPath( null, pathParts );
	}

	@Override
	public DomainReferenceBinding resolvePath(DomainReferenceBinding lhs, String... pathParts) {
		return resolveTreatedPath( lhs, null, pathParts );
	}

	private String[] sansFirstElement(String[] pathParts) {
		assert pathParts.length > 1;

		final String[] result = new String[pathParts.length - 1];
		System.arraycopy( pathParts, 1, result, 0, result.length );
		return result;
	}

	@Override
	public DomainReferenceBinding resolveTreatedPath(EntityReference subclassIndicator, String... pathParts) {
		assert pathParts.length > 0;

		// The given pathParts indicate either:
		//		* a dot-identifier sequence whose root could either be
		//			* an identification variable
		//			* an attribute name exposed from a FromElement
		//		* a single identifier which could represent:
		//			*  an identification variable
		//			* an attribute name exposed from a FromElement

		if ( pathParts.length > 1 ) {
			// we had a dot-identifier sequence...

			// see if the root is an identification variable
			final DomainReferenceBinding identifiedBinding = context().getFromElementLocator().findFromElementByIdentificationVariable( pathParts[0] );
			if ( identifiedBinding != null ) {
				validatePathRoot( identifiedBinding );
				return resolveTreatedPath( identifiedBinding, subclassIndicator, sansFirstElement( pathParts ) );
			}

			// otherwise see if the root might be the name of an attribute exposed from a FromElement
			final DomainReferenceBinding root = context().getFromElementLocator().findFromElementExposingAttribute( pathParts[0] );
			if ( root != null ) {
				validatePathRoot( root );
				return resolveTreatedPath( root, subclassIndicator, pathParts );
			}
		}
		else {
			// we had a single identifier...

			// see if the identifier is an identification variable
			final DomainReferenceBinding identifiedFromElement = context().getFromElementLocator()
					.findFromElementByIdentificationVariable( pathParts[0] );
			if ( identifiedFromElement != null ) {
				return resolveFromElementAliasAsTerminal( identifiedFromElement );
			}

			// otherwise see if the identifier might be the name of an attribute exposed from a FromElement
			final DomainReferenceBinding root = context().getFromElementLocator().findFromElementExposingAttribute( pathParts[0] );
			if ( root != null ) {
				// todo : consider passing along subclassIndicator
				return resolveTerminalAttributeBinding( root, pathParts[0] );
			}
		}

		return null;
	}

	protected void validatePathRoot(DomainReferenceBinding root) {
	}

	@Override
	public DomainReferenceBinding resolveTreatedPath(
			DomainReferenceBinding lhs,
			EntityReference subclassIndicator,
			String... pathParts) {
		final DomainReferenceBinding lhsBinding = resolveAnyIntermediateAttributePathJoins( lhs, pathParts );
		return resolveTerminalAttributeBinding( lhsBinding, pathParts[pathParts.length-1] );
	}

	protected AttributeBinding resolveTerminalAttributeBinding(
			DomainReferenceBinding lhs,
			String terminalName) {
		final AttributeReference attribute = resolveAttributeDescriptor( lhs, terminalName );
		if ( shouldRenderTerminalAttributeBindingAsJoin() && isJoinable( attribute ) ) {
			log.debugf(
					"Resolved terminal attribute-binding [%s.%s ->%s] as attribute-join",
					lhs.getFromElement().asLoggableText(),
					terminalName,
					attribute
			);
			return buildAttributeJoin(
					// see note in #resolveTreatedTerminal regarding cast
					lhs,
					attribute,
					null
			);
		}
		else {
			log.debugf(
					"Resolved terminal attribute-binding [%s.%s ->%s] as attribute-reference",
					lhs.getFromElement().asLoggableText(),
					terminalName,
					attribute
			);
			return context().getParsingContext().findOrCreateAttributeBinding(
					lhs,
					attribute
			);
		}
	}

	private boolean isJoinable(AttributeReference attribute) {
		if ( SingularAttributeReference.class.isInstance( attribute ) ) {
			final SingularAttributeReference attrRef = (SingularAttributeReference) attribute;
			return attrRef.getAttributeTypeClassification() == SingularAttributeClassification.EMBEDDED
					|| attrRef.getAttributeTypeClassification() == SingularAttributeClassification.MANY_TO_ONE
					|| attrRef.getAttributeTypeClassification() == SingularAttributeClassification.ONE_TO_ONE;
		}
		else {
			// plural attributes can always be joined.
			return true;
		}
	}

	protected DomainReferenceBinding resolveFromElementAliasAsTerminal(DomainReferenceBinding aliasedBinding) {
		log.debugf( "Resolved terminal as from-element alias : %s", aliasedBinding.getFromElement().getIdentificationVariable() );
		return aliasedBinding;
	}

	protected DomainReferenceBinding resolveTreatedTerminal(
			ResolutionContext context,
			DomainReferenceBinding lhs,
			String terminalName,
			EntityReference subclassIndicator) {
		final AttributeReference joinedAttribute = resolveAttributeDescriptor( lhs.getFromElement(), terminalName );
		log.debugf( "Resolved terminal treated-path : %s -> %s", joinedAttribute, subclassIndicator );
		final AttributeBinding joinBinding = buildAttributeJoin(
				// todo : just do a cast for now, but this needs to be thought out (Binding -> SqmFrom)
				//		^^ SqmFrom specifically needed mainly needed for "FromElementSpace"
				//		but perhaps that resolution could be delayed
				lhs,
				joinedAttribute,
				subclassIndicator
		);

		joinBinding.getFromElement().addDowncast( new Downcast( subclassIndicator ) );

		return new TreatedFromElementBinding( joinBinding, subclassIndicator );
	}
}
