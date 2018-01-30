/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;

import org.hibernate.sqm.parser.InterpretationException;
import org.hibernate.sqm.parser.QueryException;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.criteria.internal.CriteriaInterpreter;
import org.hibernate.query.sqm.produce.spi.HqlParseTreeBuilder;
import org.hibernate.query.sqm.produce.spi.SemanticQueryBuilder;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser;
import org.hibernate.sqm.query.SqmDeleteStatement;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.SqmUpdateStatement;

/**
 * Main entry point into building semantic queries.
 *
 * @author Steve Ebersole
 */
public class SemanticQueryInterpreter {
	/**
	 * Performs the interpretation of a HQL/JPQL query string to SQM.
	 *
	 * @param query The HQL/JPQL query string to interpret
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming query.
	 */
	public static SqmStatement interpret(String query, ConsumerContext consumerContext) {
		final ParsingContext parsingContext = new ParsingContext( consumerContext );

		// first, ask Antlr to build the parse tree
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( query );

		// then we perform semantic analysis and build the semantic representation...
		try {
			return SemanticQueryBuilder.buildSemanticModel( parser.statement(), parsingContext );
		}
		catch (QueryException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InterpretationException( query, e );
		}
	}

	/**
	 * Perform the interpretation of a (select) criteria query.
	 *
	 * @param query The criteria query
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming criteria query.
	 */
	public static SqmSelectStatement interpret(CriteriaQuery query, ConsumerContext consumerContext) {
		try {
			return CriteriaInterpreter.interpretSelectCriteria( query, new ParsingContext( consumerContext ) );
		}
		catch (QueryException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InterpretationException( "<criteria>", e );
		}
	}

	/**
	 * Perform the interpretation of a (delete) criteria query.
	 *
	 * @param query The criteria query
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming criteria query.
	 */
	public static SqmDeleteStatement interpret(CriteriaDelete query, ConsumerContext consumerContext) {
		throw new NotYetImplementedException();
	}

	/**
	 * Perform the interpretation of a (update) criteria query.
	 *
	 * @param query The criteria query
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming criteria query.
	 */
	public static SqmUpdateStatement interpret(CriteriaUpdate query, ConsumerContext consumerContext) {
		throw new NotYetImplementedException();
	}
}
