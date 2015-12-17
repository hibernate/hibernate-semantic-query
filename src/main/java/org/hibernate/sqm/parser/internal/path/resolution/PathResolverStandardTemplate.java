/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.path.resolution;

import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.path.FromElementBinding;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.from.Downcast;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class PathResolverStandardTemplate extends AbstractPathResolverImpl {
	private static final Logger log = Logger.getLogger( PathResolverStandardTemplate.class );

	protected abstract FromElement findFromElementByAlias(String alias);

	protected abstract FromElement findFromElementWithAttribute(String attributeName);

	@Override
	public Binding resolvePath(HqlParser.DotIdentifierSequenceContext path) {
		return resolvePath( path, null );
	}


	@Override
	public Binding resolvePath(HqlParser.DotIdentifierSequenceContext path, EntityType subclassIndicator) {
		final String pathText = path.getText();
		log.debugf( "Starting resolution of dot-ident sequence : %s", pathText );

		final String[] parts = pathText.split( "\\." );

		final String rootPart = parts[0];

		// 1st level precedence : qualified-attribute-path
		if ( pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				validatePathRoot( aliasedFromElement );
				final AttributeBindingSource terminalLhs = resolveAnyIntermediateAttributePathJoins( aliasedFromElement, parts, 1 );
				return resolveTerminalAttributeBinding( terminalLhs, parts[parts.length-1] );
			}
		}

		// 2nd level precedence : from-element alias
		if ( !pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				return resolveFromElementAliasAsTerminal( aliasedFromElement );
			}
		}

		// 3rd level precedence : unqualified-attribute-path
		final FromElement root = findFromElementWithAttribute( rootPart );
		if ( root != null ) {
			validatePathRoot( root );
			final AttributeBindingSource terminalLhs = resolveAnyIntermediateAttributePathJoins( root, parts, 0 );
			return resolveTerminalAttributeBinding( terminalLhs, parts[parts.length-1] );
		}

		return null;
	}

	protected void validatePathRoot(FromElement root) {
	}

	protected AttributeBinding resolveTerminalAttributeBinding(AttributeBindingSource lhs, String terminalName) {
		final Attribute attribute = resolveAttributeDescriptor( lhs, terminalName );
		log.debugf( "Resolved terminal attribute-binding [%s -> %s] : %s", lhs.asLoggableText(), terminalName, attribute );
		return new AttributeReferenceExpression( lhs, attribute );
	}

	protected FromElementBinding resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
		log.debugf( "Resolved terminal as from-element alias : %s", aliasedFromElement.getIdentificationVariable() );
		return aliasedFromElement;
	}

	protected FromElementBinding resolveTreatedTerminal(AttributeBindingSource lhs, String terminalName, EntityType subclassIndicator) {
		final Attribute joinedAttribute = resolveAttributeDescriptor( lhs, terminalName );
		log.debugf( "Resolved terminal treated-path : %s -> %s", joinedAttribute, subclassIndicator );
		final QualifiedAttributeJoinFromElement join = buildAttributeJoin( lhs.getFromElement(), joinedAttribute, subclassIndicator );

		join.addDowncast( new Downcast( subclassIndicator ) );

		return new TreatedFromElement( join, subclassIndicator );
	}
}
