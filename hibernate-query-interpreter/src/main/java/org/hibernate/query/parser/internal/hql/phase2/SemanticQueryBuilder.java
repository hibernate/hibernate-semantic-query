/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.phase2;

import org.hibernate.query.parser.NotYetImplementedException;
import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.StrictJpaComplianceViolation;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseProcessor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.query.parser.internal.hql.path.BasicAttributePathResolverImpl;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.expression.FunctionExpression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.order.OrderByClause;

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

	@Override
	public FunctionExpression visitNonStandardFunction(HqlParser.NonStandardFunctionContext ctx) {
		if ( getParsingContext().getConsumerContext().useStrictJpaCompliance() ) {
			throw new StrictJpaComplianceViolation(
					"Encountered non-compliant non-standard function call [" +
							ctx.nonStandardFunctionName() + "], but strict JPQL compliance was requested",
					StrictJpaComplianceViolation.Type.FUNCTION_CALL
			);
		}
		return super.visitNonStandardFunction( ctx );
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
						fromClauseProcessor.getFromElementBuilder(),
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
}
