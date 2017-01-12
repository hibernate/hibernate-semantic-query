/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.hql.internal.navigable;

import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.SqmSingularAttribute;
import org.hibernate.sqm.domain.SqmSingularAttribute.SingularAttributeClassification;
import org.hibernate.sqm.parser.common.ResolutionContext;
import org.hibernate.sqm.query.expression.domain.SqmAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.query.from.SqmDowncast;
import org.hibernate.sqm.query.from.SqmFromExporter;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class PathResolverBasicImpl extends AbstractNavigableBindingResolver {
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
	public SqmNavigableBinding resolvePath(String... pathParts) {
		return resolveTreatedPath( null, pathParts );
	}

	@Override
	public SqmNavigableBinding resolvePath(SqmNavigableSourceBinding sourceBinding, String... pathParts) {
		return resolveTreatedPath( sourceBinding, null, pathParts );
	}

	private String[] sansFirstElement(String[] pathParts) {
		assert pathParts.length > 1;

		final String[] result = new String[pathParts.length - 1];
		System.arraycopy( pathParts, 1, result, 0, result.length );
		return result;
	}

	@Override
	public SqmNavigableBinding resolveTreatedPath(SqmExpressableTypeEntity subclassIndicator, String... pathParts) {
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
			final SqmNavigableSourceBinding identifiedBinding = (SqmNavigableSourceBinding) context().getFromElementLocator()
					.findNavigableBindingByIdentificationVariable( pathParts[0] );
			if ( identifiedBinding != null ) {
				validatePathRoot( identifiedBinding );
				return resolveTreatedPath( identifiedBinding, subclassIndicator, sansFirstElement( pathParts ) );
			}

			// otherwise see if the root might be the name of an attribute exposed from a FromElement
			final SqmNavigableSourceBinding root = (SqmNavigableSourceBinding) context().getFromElementLocator()
					.findNavigableBindingExposingAttribute( pathParts[0] );
			if ( root != null ) {
				validatePathRoot( root );
				return resolveTreatedPath( root, subclassIndicator, pathParts );
			}
		}
		else {
			// we had a single identifier...

			// see if the identifier is an identification variable
			final SqmNavigableBinding identifiedFromElement = context().getFromElementLocator()
					.findNavigableBindingByIdentificationVariable( pathParts[0] );
			if ( identifiedFromElement != null ) {
				return resolveFromElementAliasAsTerminal( (SqmFromExporter) identifiedFromElement );
			}

			// otherwise see if the identifier might be the name of an attribute exposed from a FromElement
			final SqmNavigableBinding root = context().getFromElementLocator().findNavigableBindingExposingAttribute( pathParts[0] );
			if ( root != null ) {
				// todo : consider passing along subclassIndicator
				return resolveTerminalAttributeBinding( (SqmNavigableSourceBinding) root, pathParts[0], subclassIndicator );
			}
		}

		return null;
	}

	protected void validatePathRoot(SqmNavigableBinding root) {
	}

	@Override
	public SqmNavigableBinding resolveTreatedPath(
			SqmNavigableSourceBinding sourceBinding,
			SqmExpressableTypeEntity subclassIndicator,
			String... pathParts) {
		final SqmNavigableSourceBinding intermediateJoinBindings = resolveAnyIntermediateAttributePathJoins( sourceBinding, pathParts );
		return resolveTerminalAttributeBinding( intermediateJoinBindings, pathParts[pathParts.length-1] );
	}

	protected SqmNavigableBinding resolveTerminalAttributeBinding(
			SqmNavigableSourceBinding sourceBinding,
			String terminalName) {
		return resolveTerminalAttributeBinding(
				sourceBinding,
				terminalName,
				null
		);
	}

	protected SqmNavigableBinding resolveTerminalAttributeBinding(
			SqmNavigableSourceBinding sourceBinding,
			String terminalName,
			SqmExpressableTypeEntity intrinsicSubclassIndicator) {
		final SqmNavigable attribute = resolveNavigable( sourceBinding, terminalName );
		if ( shouldRenderTerminalAttributeBindingAsJoin() && isJoinable( attribute ) ) {
			log.debugf(
					"Resolved terminal attribute-binding [%s.%s ->%s] as attribute-join",
					sourceBinding.asLoggableText(),
					terminalName,
					attribute
			);
			return buildAttributeJoin(
					// see note in #resolveTreatedTerminal regarding cast
					sourceBinding,
					attribute,
					intrinsicSubclassIndicator
			);
		}
		else {
			log.debugf(
					"Resolved terminal attribute-binding [%s.%s ->%s] as attribute-reference",
					sourceBinding.asLoggableText(),
					terminalName,
					attribute
			);

			// todo (6.0) : we should probably force these to forcefully resolve the join.
			return context().getParsingContext().findOrCreateNavigableBinding(
					sourceBinding,
					attribute
			);
		}
	}

	private boolean isJoinable(SqmNavigable attribute) {
		if ( SqmSingularAttribute.class.isInstance( attribute ) ) {
			final SqmSingularAttribute attrRef = (SqmSingularAttribute) attribute;
			return attrRef.getAttributeTypeClassification() == SingularAttributeClassification.EMBEDDED
					|| attrRef.getAttributeTypeClassification() == SingularAttributeClassification.MANY_TO_ONE
					|| attrRef.getAttributeTypeClassification() == SingularAttributeClassification.ONE_TO_ONE;
		}
		else {
			// plural attributes can always be joined.
			return true;
		}
	}

	protected SqmNavigableBinding resolveFromElementAliasAsTerminal(SqmFromExporter exporter) {
		log.debugf(
				"Resolved terminal as from-element alias : %s",
				exporter.getExportedFromElement().getIdentificationVariable()
		);
		return exporter.getExportedFromElement().getBinding();
	}

	protected SqmNavigableBinding resolveTreatedTerminal(
			ResolutionContext context,
			SqmNavigableSourceBinding lhs,
			String terminalName,
			SqmExpressableTypeEntity subclassIndicator) {
		final SqmNavigable joinedAttribute = resolveNavigable( lhs, terminalName );
		log.debugf( "Resolved terminal treated-path : %s -> %s", joinedAttribute, subclassIndicator );
		final SqmAttributeBinding joinBinding = (SqmAttributeBinding) buildAttributeJoin(
				// todo : just do a cast for now, but this needs to be thought out (Binding -> SqmFrom)
				//		^^ SqmFrom specifically needed mainly needed for "FromElementSpace"
				//		but perhaps that resolution could be delayed
				lhs,
				joinedAttribute,
				subclassIndicator
		);

		joinBinding.addDowncast( new SqmDowncast( subclassIndicator ) );

		return new TreatedNavigableBinding( joinBinding, subclassIndicator );
	}
}
