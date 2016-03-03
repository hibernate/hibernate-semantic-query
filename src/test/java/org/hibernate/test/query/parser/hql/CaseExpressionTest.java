/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.CaseSearchedExpression;
import org.hibernate.sqm.query.expression.CoalesceExpression;
import org.hibernate.sqm.query.expression.LiteralStringExpression;
import org.hibernate.sqm.query.expression.CaseSimpleExpression;
import org.hibernate.sqm.query.expression.NullifExpression;
import org.hibernate.sqm.query.predicate.RelationalPredicate;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class CaseExpressionTest {
	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Test
	public void testBasicSimpleCaseExpression() {
		SelectStatement select = (SelectStatement) interpret( "select e from Entity e where e.basic2 = case e.basic when 1 then 'Steve' else 'Jon' end", consumerContext );

		assertThat( select.getQuerySpec().getWhereClause().getPredicate(), instanceOf( RelationalPredicate.class ) );
		RelationalPredicate predicate = (RelationalPredicate) select.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.getRightHandExpression(), instanceOf( CaseSimpleExpression.class ) );
		CaseSimpleExpression caseStatement = (CaseSimpleExpression) predicate.getRightHandExpression();
		assertThat( caseStatement.getFixture(), notNullValue() );
		assertThat( caseStatement.getFixture(), instanceOf( AttributeReferenceExpression.class ) );

		assertThat( caseStatement.getOtherwise(), notNullValue() );
		assertThat( caseStatement.getOtherwise(), instanceOf( LiteralStringExpression.class ) );

		assertThat( caseStatement.getWhenFragments().size(), is(1) );
	}

	@Test
	public void testBasicSearchedCaseExpression() {
		SelectStatement select = (SelectStatement) interpret( "select e from Entity e where e.basic2 = case when e.basic=1 then 'Steve' else 'Jon' end", consumerContext );

		assertThat( select.getQuerySpec().getWhereClause().getPredicate(), instanceOf( RelationalPredicate.class ) );
		RelationalPredicate predicate = (RelationalPredicate) select.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.getRightHandExpression(), instanceOf( CaseSearchedExpression.class ) );
		CaseSearchedExpression caseStatement = (CaseSearchedExpression) predicate.getRightHandExpression();

		assertThat( caseStatement.getOtherwise(), notNullValue() );
		assertThat( caseStatement.getOtherwise(), instanceOf( LiteralStringExpression.class ) );

		assertThat( caseStatement.getWhenFragments().size(), is(1) );
	}

	@Test
	public void testBasicCoalesceExpression() {
		SelectStatement select = (SelectStatement) interpret( "select coalesce(e.basic2, e.basic3, e.basic4) from Entity e", consumerContext );

		assertThat( select.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		assertThat(
				select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( CoalesceExpression.class )
		);

		CoalesceExpression coalesce = (CoalesceExpression) select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( coalesce.getValues().size(), is(3) );

		assertThat( coalesce.getExpressionType().getTypeName(), is( String.class.getName() ) );
	}

	@Test
	public void testBasicNullifExpression() {
		SelectStatement select = (SelectStatement) interpret( "select nullif(e.basic2, e.basic3) from Entity e", consumerContext );

		assertThat( select.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		assertThat(
				select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( NullifExpression.class )
		);

		NullifExpression nullif = (NullifExpression) select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();

		assertThat( nullif.getExpressionType().getTypeName(), is( String.class.getName() ) );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		entityType.makeSingularAttribute(
				"basic",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic2",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic3",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic4",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"from",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"select",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		EntityTypeImpl entity2Type = metamodel.makeEntityType( "com.acme.Entity2" );
		entity2Type.makeSingularAttribute(
				"basic1",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		EntityTypeImpl legType = metamodel.makeEntityType( "com.acme.Leg" );

		EntityTypeImpl tripType = metamodel.makeEntityType( "com.acme.Trip" );
		tripType.makeMapAttribute(
				"mapLegs",
				StandardBasicTypeDescriptors.INSTANCE.STRING,
				legType
		);
		tripType.makeListAttribute(
				"collectionLegs",
				StandardBasicTypeDescriptors.INSTANCE.INTEGER,
				legType
		);

		return metamodel;
	}
}
