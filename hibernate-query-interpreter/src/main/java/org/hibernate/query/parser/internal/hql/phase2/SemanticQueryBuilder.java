/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.phase2;

import org.hibernate.query.parser.NotYetImplementedException;
import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseProcessor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.query.parser.internal.hql.path.BasicAttributePathResolverImpl;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.from.FromClause;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SemanticQueryBuilder extends AbstractHqlParseTreeVisitor {
	private static final Logger log = Logger.getLogger( SemanticQueryBuilder.class );

	private final FromClauseProcessor fromClauseProcessor;

	private FromClauseStackNode currentFromClauseNode;

	public SemanticQueryBuilder(ParsingContext parsingContext, FromClauseProcessor fromClauseProcessor) {
		super( parsingContext, fromClauseProcessor.getFromElementBuilder(), fromClauseProcessor.getFromClauseIndex() );
		this.fromClauseProcessor = fromClauseProcessor;

		if ( fromClauseProcessor.getStatementType() == Statement.Type.INSERT ) {
			throw new NotYetImplementedException();
			// set currentFromClause
		}
		else if ( fromClauseProcessor.getStatementType() == Statement.Type.UPDATE ) {
			throw new NotYetImplementedException();
			// set currentFromClause
		}
		else if ( fromClauseProcessor.getStatementType() == Statement.Type.DELETE ) {
			throw new NotYetImplementedException();
			// set currentFromClause
		}
	}

	@Override
	public FromClause getCurrentFromClause() {
		return currentFromClauseNode.getFromClause();
	}

	@Override
	public FromClauseStackNode getCurrentFromClauseNode() {
		return currentFromClauseNode;
	}

	@Override
	public Statement visitStatement(HqlParser.StatementContext ctx) {
		// for the moment, only selectStatements are valid...
		return visitSelectStatement( ctx.selectStatement() );
	}

	@Override
	public QuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
		final FromClauseStackNode fromClauseNode = fromClauseProcessor.findFromClauseForQuerySpec( ctx );
		if ( fromClauseNode == null ) {
			throw new ParsingException( "Could not resolve FromClause by QuerySpecContext" );
		}
		FromClauseStackNode originalCurrentFromClauseNode = currentFromClauseNode;
		currentFromClauseNode = fromClauseNode;
		attributePathResolverStack.push(
				new BasicAttributePathResolverImpl(
						fromClauseProcessor.getFromElementBuilder(),
						fromClauseProcessor.getFromClauseIndex(),
						getParsingContext(),
						currentFromClauseNode
				)
		);
		try {
			return super.visitQuerySpec( ctx );
		}
		finally {
			attributePathResolverStack.pop();
			currentFromClauseNode = originalCurrentFromClauseNode;
		}
	}

	@Override
	public AttributePathPart visitIndexedPath(HqlParser.IndexedPathContext ctx) {
		return super.visitIndexedPath( ctx );
	}
}
