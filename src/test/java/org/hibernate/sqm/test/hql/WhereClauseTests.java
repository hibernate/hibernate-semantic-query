/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.domain.SqmPluralAttributeIndex.IndexClassification;
import org.hibernate.query.sqm.domain.SqmPluralAttribute;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.CollectionSizeSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralIntegerSqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.query.sqm.tree.predicate.NullnessSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.RelationalPredicateOperator;
import org.hibernate.query.sqm.tree.predicate.RelationalSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
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
				func.getPluralAttributeBinding().getSourceReference().getExportedFromElement().getIdentificationVariable(),
				is( "t" )
		);
		assertThat(
				func.getPluralAttributeBinding().getReferencedNavigable().getAttributeName(),
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

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( SqmCollectionIndexReference.class ) );
		final SqmPluralAttributeReference collectionBinding = ( (SqmCollectionIndexReference) relationalPredicate.getLeftHandExpression() ).getSourceReference();
		assertThat( collectionBinding.getExportedFromElement().getIdentificationVariable(), is( "l" ) );
	}

	@Test
	public void testMapKeyFunction() {
		SqmSelectStatement statement = interpretSelect( "select e from EntityOfMaps e join e.basicToBasicMap m where key(m) = 'foo'" );

		SqmPredicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate relationalPredicate = ( (RelationalSqmPredicate) predicate );

		final SqmCollectionIndexReference collectionIndexBinding = (SqmCollectionIndexReference) relationalPredicate.getLeftHandExpression();
		final SqmPluralAttributeReference collectionBinding = collectionIndexBinding.getSourceReference();
		final SqmPluralAttribute attributeReference = collectionBinding.getReferencedNavigable();

		assertThat( collectionIndexBinding.getExpressionType(), is( attributeReference.getIndexDescriptor() ) );

		assertThat( attributeReference.getIndexDescriptor().getClassification(), is( IndexClassification.BASIC ) );
		assertEquals( String.class, attributeReference.getIndexDescriptor().getExportedDomainType().getJavaType() );

		assertThat( collectionBinding.getExportedFromElement().getIdentificationVariable(), is( "m" ) );
	}
}
