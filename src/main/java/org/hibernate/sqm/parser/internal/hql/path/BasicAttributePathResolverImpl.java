/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.path;

import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.internal.FromClauseIndex;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.from.FromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class BasicAttributePathResolverImpl extends StandardAttributePathResolverTemplate {
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
		return fromElementBuilder.getAliasRegistry().findFromElementByAlias( alias );
	}

	protected FromElement findFromElementWithAttribute(String attributeName) {
		return fromClauseIndex.findFromElementWithAttribute( fromClause, attributeName );
	}

	protected AttributePathPart resolveTerminalPathPart(FromElement lhs, String terminalName) {
		final AttributeReferenceExpression expr = makeAttributeReferenceExpression( lhs, terminalName );
		log.debugf( "Resolved terminal path-part [%s] : %s", terminalName, expr );
		return expr;
	}

	protected AttributeReferenceExpression makeAttributeReferenceExpression(FromElement lhs, String attributeName) {
		final Attribute attribute = lhs.resolveAttribute( attributeName );
		final Type type;
		if ( attribute instanceof SingularAttribute ) {
			type = ( (SingularAttribute) attribute ).getType();
		}
		else if ( attribute instanceof PluralAttribute ) {
			type = ( (PluralAttribute) attribute ).getElementType();
		}
		else {
			throw new ParsingException(
					"Resolved attribute was neither javax.persistence.metamodel.SingularAttribute " +
							"nor javax.persistence.metamodel.PluralAttribute : " +
							attribute
			);
		}

		return new AttributeReferenceExpression( lhs, attribute, type );
	}

	protected AttributePathPart resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
		log.debugf( "Resolved from-element alias as terminal : %s", aliasedFromElement.getAlias() );
		return new FromElementReferenceExpression( aliasedFromElement, aliasedFromElement.getBindableModelDescriptor().getBoundType() );
	}
}
