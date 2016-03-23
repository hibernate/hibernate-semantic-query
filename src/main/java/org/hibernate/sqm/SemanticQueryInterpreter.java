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
import org.hibernate.sqm.parser.hql.internal.HqlParseTreeBuilder;
import org.hibernate.sqm.parser.hql.internal.SemanticQueryBuilder;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;

/**
 * Main entry point into building semantic queries.
 *
 * @author Steve Ebersole
 */
public class SemanticQueryInterpreter {
	/**
	 * Performs the interpretation of a HQL/JPQL sqm string.
	 *
	 * @param query The HQL/JPQL sqm to interpret
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming sqm.
	 */
	public static Statement interpret(String query, ConsumerContext consumerContext) {
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
	 * Perform the interpretation of a (select) criteria sqm.
	 *
	 * @param query The criteria sqm
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming sqm.
	 */
	public static SelectStatement interpret(CriteriaQuery query, ConsumerContext consumerContext) {
		return CriteriaInterpreter.interpretSelectCriteria( query, new ParsingContext( consumerContext ) );
	}

	/**
	 * Perform the interpretation of a (delete) criteria sqm.
	 *
	 * @param query The criteria sqm
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming sqm.
	 */
	public static DeleteStatement interpret(CriteriaDelete query, ConsumerContext consumerContext) {
		throw new NotYetImplementedException();
	}

	/**
	 * Perform the interpretation of a (update) criteria sqm.
	 *
	 * @param query The criteria sqm
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming sqm.
	 */
	public static UpdateStatement interpret(CriteriaUpdate query, ConsumerContext consumerContext) {
		throw new NotYetImplementedException();
	}
}
