/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.path;

import javax.persistence.metamodel.Attribute;

import org.hibernate.query.parser.internal.Helper;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.from.FromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class StandardAttributePathResolverTemplate extends AbstractAttributePathResolverImpl {
	private static final Logger log = Logger.getLogger( StandardAttributePathResolverTemplate.class );

	protected abstract FromElement findFromElementByAlias(String alias);

	protected abstract FromElement findFromElementWithAttribute(String attributeName);

	@Override
	public AttributePathPart resolvePath(HqlParser.DotIdentifierSequenceContext path) {
		final String pathText = path.getText();
		log.debugf( "Starting resolution of dot-ident sequence : %s", pathText );

		final String[] parts = pathText.split( "\\." );

		final String rootPart = parts[0];

		// 1st level precedence : qualified-attribute-path
		if ( pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				validatePathRoot( aliasedFromElement );
				final FromElement lhs = resolveAnyIntermediateAttributePathJoins( aliasedFromElement, parts, 1 );
				return resolveTerminalPathPart( lhs, parts[parts.length-1] );
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
			final FromElement lhs = resolveAnyIntermediateAttributePathJoins( root, parts, 0 );
			return resolveTerminalPathPart( lhs, parts[parts.length-1] );
		}

		return null;
	}

	protected void validatePathRoot(FromElement root) {
	}

	protected AttributePathPart resolveTerminalPathPart(FromElement lhs, String terminalName) {
		final AttributeReferenceExpression expr = makeAttributeReferenceExpression( lhs, terminalName );
		log.debugf( "Resolved terminal path-part [%s] : %s", terminalName, expr );
		return expr;
	}

	protected AttributePathPart resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
		log.debugf( "Resolved from-element alias as terminal : %s", aliasedFromElement.getAlias() );
		return new FromElementReferenceExpression( aliasedFromElement, aliasedFromElement.getBindableModelDescriptor().getBoundType() );
	}
}
