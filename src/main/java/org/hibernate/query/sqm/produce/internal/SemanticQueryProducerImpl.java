/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.internal;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.InterpretationException;
import org.hibernate.query.sqm.QueryException;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.produce.internal.criteria.CriteriaInterpreter;
import org.hibernate.query.sqm.produce.internal.hql.HqlParseTreeBuilder;
import org.hibernate.query.sqm.produce.internal.hql.SemanticQueryBuilder;
import org.hibernate.query.sqm.hql.internal.antlr.HqlParser;
import org.hibernate.query.sqm.produce.spi.ConsumerContext;
import org.hibernate.query.sqm.produce.spi.SemanticQueryProducer;
import org.hibernate.query.sqm.tree.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.SqmUpdateStatement;

/**
 * Standard implementation of SemanticQueryInterpreter
 *
 * @author Steve Ebersole
 */
public class SemanticQueryProducerImpl implements SemanticQueryProducer {
	private final SessionFactoryImplementor sessionFactory;

	public SemanticQueryProducerImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Performs the interpretation of a HQL/JPQL query string to SQM.
	 *
	 * @param query The HQL/JPQL query string to interpret
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming query.
	 */
	public SqmStatement interpret(String query, ConsumerContext consumerContext) {
		final ParsingContext parsingContext = new ParsingContext( sessionFactory );

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
	public SqmSelectStatement interpret(CriteriaQuery query, ConsumerContext consumerContext) {
		try {
			return CriteriaInterpreter.interpretSelectCriteria( query, new ParsingContext( sessionFactory ) );
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
	 * @param criteria The DELETE criteria
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming criteria query.
	 */
	public SqmDeleteStatement interpret(CriteriaDelete criteria, ConsumerContext consumerContext) {
		try {
			return CriteriaInterpreter.interpretDeleteCriteria( criteria, new ParsingContext( sessionFactory ) );
		}
		catch (QueryException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InterpretationException( "<criteria>", e );
		}
	}

	/**
	 * Perform the interpretation of a (update) criteria query.
	 *
	 * @param criteria The criteria query
	 * @param consumerContext Callback information
	 *
	 * @return The semantic representation of the incoming criteria query.
	 */
	public SqmUpdateStatement interpret(CriteriaUpdate criteria, ConsumerContext consumerContext) {
		try {
			return CriteriaInterpreter.interpretUpdateCriteria( criteria, new ParsingContext( sessionFactory ) );
		}
		catch (QueryException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InterpretationException( "<criteria>", e );
		}
	}
}
