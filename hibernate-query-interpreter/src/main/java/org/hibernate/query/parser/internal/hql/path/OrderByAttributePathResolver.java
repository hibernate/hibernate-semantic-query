/*
* Hibernate, Relational Persistence for Idiomatic Java
*
* License: Apache License, Version 2.0
* See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
*/
package org.hibernate.query.parser.internal.hql.path;

import org.jboss.logging.Logger;

import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.expression.ResultVariableReferenceExpression;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.select.Selection;

/**
 * @author Andrea Boriero
 */
public class OrderByAttributePathResolver extends AbstractAttributePathResolverImpl {
	private static final Logger log = Logger.getLogger( OrderByAttributePathResolver.class );

	private final FromElementBuilder fromElementBuilder;
	private final FromClauseIndex fromClauseIndex;
	private final ParsingContext parsingContext;
	private final FromClauseStackNode fromClause;

	public OrderByAttributePathResolver(
			FromElementBuilder fromElementBuilder,
			FromClauseIndex fromClauseIndex,
			ParsingContext parsingContext,
			FromClauseStackNode fromClause) {
		this.fromElementBuilder = fromElementBuilder;
		this.fromClauseIndex = fromClauseIndex;
		this.parsingContext = parsingContext;
		this.fromClause = fromClause;
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
				final FromElement lhs = resolveAnyIntermediateAttributePathJoins( aliasedFromElement, parts, 1 );
				return resolveTerminalPathPart( lhs, parts[parts.length - 1] );
			}
			else {
				Selection selectionByAlias = fromElementBuilder.getAliasRegistry().findSelectionByAlias( rootPart );
				if ( selectionByAlias != null ) {

				}
			}
		}

		// 2nd level precedence : from-element alias
		if ( !pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				return resolveFromElementAliasAsTerminal( aliasedFromElement );
			}
		}

		// 3rd level precedence :selection unqualified-attribute-path
		Selection selectionByAlias = fromElementBuilder.getAliasRegistry().findSelectionByAlias( rootPart );
		if ( selectionByAlias != null ) {
			return new ResultVariableReferenceExpression( selectionByAlias );
		}

		// 4rd level precedence : from unqualified-attribute-path
		final FromElement root = findFromElementWithAttribute( rootPart );
		if ( root != null ) {
			final FromElement lhs = resolveAnyIntermediateAttributePathJoins( root, parts, 0 );
			return resolveTerminalPathPart( lhs, parts[parts.length - 1] );
		}

		return null;
	}

	protected FromElement findFromElementByAlias(String alias) {
		return fromElementBuilder.getAliasRegistry().findFromElementByAlias( alias );
	}

	@Override
	protected FromElementBuilder fromElementBuilder() {
		return null;
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

	protected FromElement findFromElementWithAttribute(String attributeName) {
		return fromClauseIndex.findFromElementWithAttribute( fromClause, attributeName );
	}

	@Override
	protected ParsingContext parsingContext() {
		return parsingContext;
	}

}
