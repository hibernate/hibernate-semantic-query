/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test.domain;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.criteria.tree.JpaCriteriaQuery;
import org.hibernate.sqm.parser.hql.internal.HqlParseTreeBuilder;
import org.hibernate.sqm.parser.hql.internal.SemanticQueryBuilder;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.test.ConsumerContextImpl;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Base class defining a standard domain model
 *
 * @author Steve Ebersole
 */
public abstract class StandardModelTest {
	protected final ConsumerContextImpl consumerContext = new ConsumerContextImpl(
			OrmHelper.buildDomainMetamodel(
					Person.class,
					EntityOfSets.class,
					EntityOfLists.class,
					EntityOfMaps.class,
					EntityWithNonIdAttributeNamedId.class,
					NonAggregatedCompositeIdEntity.class,
					NonAggregatedCompositeIdEntityWithNonIdAttributeNamedId.class,
					KeywordCrazyEntity.class
			)
	);

	protected final CriteriaBuilderImpl criteriaBuilder = new CriteriaBuilderImpl( consumerContext );

	protected SqmSelectStatement interpretSelect(String query) {
		return (SqmSelectStatement) interpret( query );
	}

	protected SqmStatement interpret(String query) {
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( query );

		final ParsingContext parsingContext = new ParsingContext( consumerContext );
		return SemanticQueryBuilder.buildSemanticModel( parser.statement(), parsingContext );
	}

	protected SqmStatement interpret(JpaCriteriaQuery queryCriteria) {
		return SemanticQueryInterpreter.interpret( queryCriteria, consumerContext );
	}

}
