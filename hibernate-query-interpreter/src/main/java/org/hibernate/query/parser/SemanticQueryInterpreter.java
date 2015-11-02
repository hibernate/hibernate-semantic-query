/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;

import org.hibernate.query.parser.internal.criteria.OrderByProcessor;
import org.hibernate.query.parser.internal.criteria.QuerySpecProcessor;
import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.phase2.SemanticQueryBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.HqlParseTreeBuilder;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseProcessor;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;

import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Main entry point into the query parser.
 *
 * @author Steve Ebersole
 */
public class SemanticQueryInterpreter {
	/**
	 * Performs the interpretation of a HQL/JPQL query string.
	 *
	 * @param query The HQL/JPQL query to interpret
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming query.
	 */
	public static Statement interpret(String query, ConsumerContext consumerContext) {
		final ParsingContext parsingContext = new ParsingContext( consumerContext );

		// first, ask Antlr to build the parse tree
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( query );

		// then we begin semantic analysis and building the semantic representation...

		// Phase 1
		FromClauseProcessor fromClauseProcessor = new FromClauseProcessor( parsingContext );
		ParseTreeWalker.DEFAULT.walk( fromClauseProcessor, parser.statement() );
		parser.reset();

		// Phase 2
		return new SemanticQueryBuilder( parsingContext, fromClauseProcessor ).visitStatement( parser.statement() );
	}

	/**
	 * Perform the interpretation of a (select) criteria query.
	 *
	 * @param query The criteria query
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming query.
	 */
	public static SelectStatement interpret(CriteriaQuery query, ConsumerContext consumerContext) {
		final ParsingContext parsingContext = new ParsingContext( consumerContext );
		final QuerySpecProcessor rootQuerySpecProcessor = QuerySpecProcessor.buildRootQuerySpecProcessor( parsingContext );

		final SelectStatement selectStatement = new SelectStatement();
		selectStatement.applyQuerySpec( rootQuerySpecProcessor.visitQuerySpec( query ) );
		selectStatement.applyOrderByClause( OrderByProcessor.processOrderBy( rootQuerySpecProcessor, query ) );

		return selectStatement;
	}

	/**
	 * Perform the interpretation of a (delete) criteria query.
	 *
	 * @param query The criteria query
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming query.
	 */
	public static DeleteStatement interpret(CriteriaDelete query, ConsumerContext consumerContext) {
		throw new NotYetImplementedException();
	}

	/**
	 * Perform the interpretation of a (update) criteria query.
	 *
	 * @param query The criteria query
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming query.
	 */
	public static UpdateStatement interpret(CriteriaUpdate query, ConsumerContext consumerContext) {
		throw new NotYetImplementedException();
	}
}
