/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.CaseSearchedSqmExpression;
import org.hibernate.query.sqm.tree.expression.CaseSimpleSqmExpression;
import org.hibernate.query.sqm.tree.expression.CoalesceSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralIntegerSqmExpression;
import org.hibernate.query.sqm.tree.expression.NullifSqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.predicate.RelationalSqmPredicate;

import org.hibernate.sqm.test.domain.StandardModelTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class CaseSqmExpressionTest extends StandardModelTest {
	@Test
	public void testBasicSimpleCaseExpression() {
		SqmSelectStatement select = interpretSelect( "select p from Person p where p.numberOfToes = case p.name.first when 'Steve' then 5 else 6 end" );

		assertThat( select.getQuerySpec().getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) select.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.getRightHandExpression(), instanceOf( CaseSimpleSqmExpression.class ) );
		CaseSimpleSqmExpression caseStatement = (CaseSimpleSqmExpression) predicate.getRightHandExpression();
		assertThat( caseStatement.getFixture(), notNullValue() );
		assertThat( caseStatement.getFixture(), instanceOf( SqmSingularAttributeReference.class ) );

		assertThat( caseStatement.getOtherwise(), notNullValue() );
		assertThat( caseStatement.getOtherwise(), instanceOf( LiteralIntegerSqmExpression.class ) );

		assertThat( caseStatement.getWhenFragments().size(), is(1) );
	}

	@Test
	public void testBasicSearchedCaseExpression() {
		SqmSelectStatement select = interpretSelect( "select p from Person p where p.numberOfToes = case when p.name.first = 'Steve' then 5 else 6 end" );

		assertThat( select.getQuerySpec().getWhereClause().getPredicate(), instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) select.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.getRightHandExpression(), instanceOf( CaseSearchedSqmExpression.class ) );
		CaseSearchedSqmExpression caseStatement = (CaseSearchedSqmExpression) predicate.getRightHandExpression();

		assertThat( caseStatement.getOtherwise(), notNullValue() );
		assertThat( caseStatement.getOtherwise(), instanceOf( LiteralIntegerSqmExpression.class ) );

		assertThat( caseStatement.getWhenFragments().size(), is(1) );
	}

	@Test
	public void testBasicCoalesceExpression() {
		SqmSelectStatement select = interpretSelect( "select coalesce(p.nickName,p.name.first,p.name.last) from Person p" );

		assertThat( select.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		assertThat(
				select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( CoalesceSqmExpression.class )
		);

		CoalesceSqmExpression coalesce = (CoalesceSqmExpression) select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( coalesce.getValues().size(), is(3) );

		assertEquals( coalesce.getExpressionType().getExportedDomainType().getJavaType(), String.class );
	}

	@Test
	public void testBasicNullifExpression() {
		SqmSelectStatement select = interpretSelect( "select nullif(p.nickName, p.name.first) from Person p" );

		assertThat( select.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		assertThat(
				select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( NullifSqmExpression.class )
		);

		NullifSqmExpression nullif = (NullifSqmExpression) select.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();

		assertEquals( nullif.getExpressionType().getExportedDomainType().getJavaType(), String.class );
	}
}
