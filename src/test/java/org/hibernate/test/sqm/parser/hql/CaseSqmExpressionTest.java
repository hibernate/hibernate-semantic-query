/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.query.expression.domain.SingularAttributeBinding;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.CaseSearchedSqmExpression;
import org.hibernate.sqm.query.expression.CaseSimpleSqmExpression;
import org.hibernate.sqm.query.expression.CoalesceSqmExpression;
import org.hibernate.sqm.query.expression.LiteralStringSqmExpression;
import org.hibernate.sqm.query.expression.NullifSqmExpression;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class CaseSqmExpressionTest {
	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Test
	public void testBasicSimpleCaseExpression() {
		SqmSelectStatement select = (SqmSelectStatement) interpret( "select e from Entity e where e.basic2 = case e.basic when 1 then 'Steve' else 'Jon' end", consumerContext );

		assertThat( select.getQuerySpec().getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) select.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.getRightHandExpression(), instanceOf( CaseSimpleSqmExpression.class ) );
		CaseSimpleSqmExpression caseStatement = (CaseSimpleSqmExpression) predicate.getRightHandExpression();
		assertThat( caseStatement.getFixture(), notNullValue() );
		assertThat( caseStatement.getFixture(), instanceOf( SingularAttributeBinding.class ) );

		assertThat( caseStatement.getOtherwise(), notNullValue() );
		assertThat( caseStatement.getOtherwise(), instanceOf( LiteralStringSqmExpression.class ) );

		assertThat( caseStatement.getWhenFragments().size(), is(1) );
	}

	@Test
	public void testBasicSearchedCaseExpression() {
		SqmSelectStatement select = (SqmSelectStatement) interpret( "select e from Entity e where e.basic2 = case when e.basic=1 then 'Steve' else 'Jon' end", consumerContext );

		assertThat( select.getQuerySpec().getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) select.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.getRightHandExpression(), instanceOf( CaseSearchedSqmExpression.class ) );
		CaseSearchedSqmExpression caseStatement = (CaseSearchedSqmExpression) predicate.getRightHandExpression();

		assertThat( caseStatement.getOtherwise(), notNullValue() );
		assertThat( caseStatement.getOtherwise(), instanceOf( LiteralStringSqmExpression.class ) );

		assertThat( caseStatement.getWhenFragments().size(), is(1) );
	}

	@Test
	public void testBasicCoalesceExpression() {
		SqmSelectStatement select = (SqmSelectStatement) interpret( "select coalesce(e.basic2, e.basic3, e.basic4) from Entity e", consumerContext );

		assertThat( select.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		assertThat(
				select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( CoalesceSqmExpression.class )
		);

		CoalesceSqmExpression coalesce = (CoalesceSqmExpression) select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( coalesce.getValues().size(), is(3) );

		assertThat( coalesce.getExpressionType().asLoggableText(), containsString( String.class.getName() ) );
	}

	@Test
	public void testBasicNullifExpression() {
		SqmSelectStatement select = (SqmSelectStatement) interpret( "select nullif(e.basic2, e.basic3) from Entity e", consumerContext );

		assertThat( select.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		assertThat(
				select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( NullifSqmExpression.class )
		);

		NullifSqmExpression nullif = (NullifSqmExpression) select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();

		assertThat( nullif.getExpressionType().asLoggableText(), containsString( String.class.getName() ) );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		entityType.makeSingularAttribute(
				"basic",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic2",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic3",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic4",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"from",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"select",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		EntityTypeImpl entity2Type = metamodel.makeEntityType( "com.acme.Entity2" );
		entity2Type.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
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
