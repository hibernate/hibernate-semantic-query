/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.phase2;

import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.path.BasicAttributePathResolverImpl;
import org.hibernate.query.parser.internal.hql.path.DmlRootAttributePathResolver;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseProcessor;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.InsertSelectStatement;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.predicate.Predicate;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SemanticQueryBuilder extends AbstractHqlParseTreeVisitor {
	private static final Logger log = Logger.getLogger( SemanticQueryBuilder.class );

	private final FromClauseProcessor fromClauseProcessor;
	private FromElementBuilder currentFromElementBuilder;

	private FromClauseStackNode currentFromClauseNode;

	public SemanticQueryBuilder(ParsingContext parsingContext, FromClauseProcessor fromClauseProcessor) {
		super( parsingContext, fromClauseProcessor.getFromClauseIndex() );
		this.fromClauseProcessor = fromClauseProcessor;
	}

	@Override
	public FromClause getCurrentFromClause() {
		return currentFromClauseNode.getFromClause();
	}

	@Override
	public FromElementBuilder getFromElementBuilder() {
		return currentFromElementBuilder;
	}

	@Override
	public FromClauseStackNode getCurrentFromClauseNode() {
		return currentFromClauseNode;
	}

	@Override
	public Statement visitStatement(HqlParser.StatementContext ctx) {
		if ( ctx.insertStatement() != null ) {
			return visitInsertStatement( ctx.insertStatement() );
		}
		else if ( ctx.updateStatement() != null ) {
			return visitUpdateStatement( ctx.updateStatement() );
		}
		else if ( ctx.deleteStatement() != null ) {
			return visitDeleteStatement( ctx.deleteStatement() );
		}
		else if ( ctx.selectStatement() != null ) {
			return visitSelectStatement( ctx.selectStatement() );
		}

		throw new ParsingException( "Unexpected statement type [not INSERT, UPDATE, DELETE or SELECT] : " + ctx.getText() );
	}

	@Override
	public QuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
		currentFromElementBuilder = fromClauseProcessor.getFromElementBuilder( ctx );
		final FromClauseStackNode fromClauseNode = fromClauseProcessor.findFromClauseForQuerySpec( ctx );
		if ( fromClauseNode == null ) {
			throw new ParsingException( "Could not resolve FromClause by QuerySpecContext" );
		}
		FromClauseStackNode originalCurrentFromClauseNode = currentFromClauseNode;
		currentFromClauseNode = fromClauseNode;
		attributePathResolverStack.push(
				new BasicAttributePathResolverImpl(
						currentFromElementBuilder,
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

	@Override
	public OrderByClause visitOrderByClause(HqlParser.OrderByClauseContext ctx) {
		// use the root FromClause (should be just one) to build an
		// AttributePathResolver for the order by clause
		if ( fromClauseProcessor.getFromClauseIndex().getFromClauseStackNodeList().isEmpty() ) {
			throw new ParsingException( "No root FromClauseStackNodes found; cannot process order-by" );
		}
		if ( fromClauseProcessor.getFromClauseIndex().getFromClauseStackNodeList().size() > 1 ) {
			throw new ParsingException( "Multiple root FromClauseStackNodes found; cannot process order-by" );
		}
		FromClauseStackNode rootFromClauseStackNode = fromClauseProcessor.getFromClauseIndex().getFromClauseStackNodeList().get( 0 );
		attributePathResolverStack.push(
				new BasicAttributePathResolverImpl(
						currentFromElementBuilder,
						fromClauseProcessor.getFromClauseIndex(),
						getParsingContext(),
						rootFromClauseStackNode
				)
		);
		try {
			// and then process the order by
			return super.visitOrderByClause( ctx );
		}
		finally {
			attributePathResolverStack.pop();
		}
	}

	// todo : the structure for handling update/delete in FromClauseProcessor (and accessing them here) needs some re-thought.
	// todo : given the current set up, need to set up the FromClauseIndex to understand the "DML root"

	@Override
	public DeleteStatement visitDeleteStatement(HqlParser.DeleteStatementContext ctx) {
		final DeleteStatement deleteStatement = new DeleteStatement( fromClauseProcessor.getDmlRoot() );

		attributePathResolverStack.push(
				new DmlRootAttributePathResolver(
						fromClauseProcessor.getDmlRoot(),
						currentFromElementBuilder,
						getParsingContext()
				)
		);
		try {
			deleteStatement.getWhereClause().setPredicate( (Predicate) ctx.whereClause().predicate().accept( this ) );
		}
		finally {
			attributePathResolverStack.pop();
		}

		return deleteStatement;
	}

	@Override
	public UpdateStatement visitUpdateStatement(HqlParser.UpdateStatementContext ctx) {
		final UpdateStatement updateStatement = new UpdateStatement( fromClauseProcessor.getDmlRoot() );

		attributePathResolverStack.push(
				new DmlRootAttributePathResolver(
						fromClauseProcessor.getDmlRoot(),
						currentFromElementBuilder,
						getParsingContext()
				)
		);
		try {
			updateStatement.getWhereClause().setPredicate( (Predicate) ctx.whereClause().predicate().accept( this ) );

			for ( HqlParser.AssignmentContext assignmentContext : ctx.setClause().assignment() ) {
				// todo : validate "state field" expression
				updateStatement.getSetClause().addAssignment(
						(AttributeReferenceExpression) attributePathResolverStack.getCurrent().resolvePath( assignmentContext.dotIdentifierSequence() ),
						(Expression) assignmentContext.expression().accept( this )
				);
			}
		}
		finally {
			attributePathResolverStack.pop();
		}

		return updateStatement;
	}

	@Override
	public InsertSelectStatement visitInsertStatement(HqlParser.InsertStatementContext ctx) {
		// for now we only support the INSERT-SELECT form
		final InsertSelectStatement insertStatement = new InsertSelectStatement( fromClauseProcessor.getDmlRoot() );

		attributePathResolverStack.push(
				new DmlRootAttributePathResolver(
						fromClauseProcessor.getDmlRoot(),
						currentFromElementBuilder,
						getParsingContext()
				)
		);
		try {
			insertStatement.setSelectQuery( visitQuerySpec( ctx.querySpec() ) );

			for ( HqlParser.DotIdentifierSequenceContext stateFieldCtx : ctx.insertSpec().targetFieldsSpec().dotIdentifierSequence() ) {
				final AttributeReferenceExpression stateField = (AttributeReferenceExpression) attributePathResolverStack.getCurrent().resolvePath( stateFieldCtx );
				// todo : validate each resolved stateField...
				insertStatement.addInsertTargetStateField( stateField );
			}
		}
		finally {
			attributePathResolverStack.pop();
		}

		return insertStatement;
	}
}
