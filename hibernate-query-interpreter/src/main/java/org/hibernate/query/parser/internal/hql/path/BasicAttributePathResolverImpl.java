/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.path;

import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.from.FromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class BasicAttributePathResolverImpl extends AbstractAttributePathResolverImpl {
	private static final Logger log = Logger.getLogger( BasicAttributePathResolverImpl.class );

	private final FromElementBuilder fromElementBuilder;
	private final FromClauseIndex fromClauseIndex;
	private final FromClauseStackNode fromClause;
	private final ParsingContext parsingContext;

	public BasicAttributePathResolverImpl(
			FromElementBuilder fromElementBuilder,
			FromClauseIndex fromClauseIndex,
			ParsingContext parsingContext,
			FromClauseStackNode fromClause) {
		this.fromElementBuilder = fromElementBuilder;
		this.fromClauseIndex = fromClauseIndex;
		this.fromClause = fromClause;
		this.parsingContext = parsingContext;
	}

	@Override
	protected FromElementBuilder fromElementBuilder() {
		return fromElementBuilder;
	}

	@Override
	protected ParsingContext parsingContext() {
		return parsingContext;
	}

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

	protected FromElement findFromElementByAlias(String alias) {
		return fromClauseIndex.findFromElementByAlias( alias );
	}

	protected FromElement findFromElementWithAttribute(String attributeName) {
		return fromClauseIndex.findFromElementWithAttribute( fromClause, attributeName );
	}

	protected AttributePathPart resolveTerminalPathPart(FromElement lhs, String terminalName) {
		final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor( terminalName );
		final AttributeReferenceExpression expr = new AttributeReferenceExpression( lhs, attributeDescriptor );
		log.debugf( "Resolved terminal path-part [%s] : %s", terminalName, expr );
		return expr;
	}

	protected AttributePathPart resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
		log.debugf( "Resolved from-element alias as terminal : %s", aliasedFromElement.getAlias() );
		return new FromElementReferenceExpression( aliasedFromElement );
	}
}
