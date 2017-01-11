/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.sqm.domain.SqmPluralAttributeIndex.IndexClassification;
import org.hibernate.sqm.domain.SqmPluralAttribute;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.CollectionSizeSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.domain.CollectionIndexBinding;
import org.hibernate.sqm.query.expression.domain.SqmPluralAttributeBinding;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalPredicateOperator;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for elements of WHERE clauses.
 *
 * @author Gunnar Morling
 */
public class WhereClauseTests extends StandardModelTest {

	@Test
	public void testIsNotNullPredicate() {
		SqmSelectStatement statement = interpretSelect( "select l from Person l where l.nickName is not null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessSqmPredicate.class ) );
		NullnessSqmPredicate predicate = (NullnessSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(true) );
	}

	@Test
	public void testNotIsNullPredicate() {
		SqmSelectStatement statement = interpretSelect( "select l from Person l where not l.nickName is null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessSqmPredicate.class ) );
		NullnessSqmPredicate predicate = (NullnessSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(true) );
	}

	@Test
	public void testNotIsNotNullPredicate() {
		SqmSelectStatement statement = interpretSelect( "select l from Person l where not l.nickName is not null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessSqmPredicate.class ) );
		NullnessSqmPredicate predicate = (NullnessSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(false) );
	}

	@Test
	public void testCollectionSizeFunction() {
		SqmSelectStatement statement = interpretSelect( "SELECT t FROM EntityOfSets t WHERE SIZE( t.setOfBasics ) = 311" );

		SqmPredicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate relationalPredicate = ( (RelationalSqmPredicate) predicate );

		assertThat( relationalPredicate.getOperator(), is( RelationalPredicateOperator.EQUAL ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerSqmExpression.class ) );
		assertThat( ( (LiteralIntegerSqmExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 311 ) );

		assertThat(
				relationalPredicate.getLeftHandExpression(),
				instanceOf( CollectionSizeSqmExpression.class )
		);
		final CollectionSizeSqmExpression func = (CollectionSizeSqmExpression) relationalPredicate.getLeftHandExpression();
		assertThat(
				func.getPluralAttributeBinding().getSourceBinding().getExportedFromElement().getIdentificationVariable(),
				is( "t" )
		);
		assertThat(
				func.getPluralAttributeBinding().getBoundNavigable().getAttributeName(),
				is( "setOfBasics" )
		);
	}

	@Test
	public void testListIndexFunction() {
		SqmSelectStatement statement = interpretSelect( "select l from EntityOfLists t join t.listOfBasics l where index(l) > 2" );

		SqmPredicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate relationalPredicate = ( (RelationalSqmPredicate) predicate );

		assertThat( relationalPredicate.getOperator(), is( RelationalPredicateOperator.GREATER_THAN ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerSqmExpression.class ) );
		assertThat( ( (LiteralIntegerSqmExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 2 ) );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( CollectionIndexBinding.class ) );
		final SqmPluralAttributeBinding collectionBinding = ( (CollectionIndexBinding) relationalPredicate.getLeftHandExpression() ).getSourceBinding();
		assertThat( collectionBinding.getExportedFromElement().getIdentificationVariable(), is( "l" ) );
	}

	@Test
	public void testMapKeyFunction() {
		SqmSelectStatement statement = interpretSelect( "select e from EntityOfMaps e join e.basicToBasicMap m where key(m) = 'foo'" );

		SqmPredicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate relationalPredicate = ( (RelationalSqmPredicate) predicate );

		final CollectionIndexBinding collectionIndexBinding = (CollectionIndexBinding) relationalPredicate.getLeftHandExpression();
		final SqmPluralAttributeBinding collectionBinding = collectionIndexBinding.getSourceBinding();
		final SqmPluralAttribute attributeReference = collectionBinding.getBoundNavigable();

		assertThat( collectionIndexBinding.getExpressionType(), is( attributeReference.getIndexReference() ) );

		assertThat( attributeReference.getIndexReference().getClassification(), is( IndexClassification.BASIC ) );
		assertEquals( String.class, attributeReference.getIndexReference().getExportedDomainType().getJavaType() );

		assertThat( collectionBinding.getExportedFromElement().getIdentificationVariable(), is( "m" ) );
	}
}
