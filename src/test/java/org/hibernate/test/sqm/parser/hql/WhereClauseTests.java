/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.domain.PluralAttributeIndexReference.IndexClassification;
import org.hibernate.sqm.parser.common.MapKeyBinding;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.CollectionIndexSqmExpression;
import org.hibernate.sqm.query.expression.CollectionSizeSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.BasicTypeImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
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
public class WhereClauseTests {

	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Test
	public void testIsNotNullPredicate() {
		SqmSelectStatement statement = interpret( "select l from Leg l where l.basicName is not null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessSqmPredicate.class ) );
		NullnessSqmPredicate predicate = (NullnessSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(true) );
	}

	@Test
	public void testNotIsNullPredicate() {
		SqmSelectStatement statement = interpret( "select l from Leg l where not l.basicName is null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessSqmPredicate.class ) );
		NullnessSqmPredicate predicate = (NullnessSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(true) );
	}

	@Test
	public void testNotIsNotNullPredicate() {
		SqmSelectStatement statement = interpret( "select l from Leg l where not l.basicName is not null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessSqmPredicate.class ) );
		NullnessSqmPredicate predicate = (NullnessSqmPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(false) );
	}

	@Test
	public void testCollectionSizeFunction() {
		SqmSelectStatement statement = interpret( "SELECT t FROM Trip t WHERE SIZE( t.basicCollection ) = 311" );

		SqmPredicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate relationalPredicate = ( (RelationalSqmPredicate) predicate );

		assertThat( relationalPredicate.getOperator(), is( RelationalSqmPredicate.Operator.EQUAL ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerSqmExpression.class ) );
		assertThat( ( (LiteralIntegerSqmExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 311 ) );

		assertThat(
				relationalPredicate.getLeftHandExpression(),
				instanceOf( CollectionSizeSqmExpression.class )
		);
		final CollectionSizeSqmExpression func = (CollectionSizeSqmExpression) relationalPredicate.getLeftHandExpression();
		assertThat(
				func.getPluralAttributeBinding().getLhs().getFromElement().getIdentificationVariable(),
				is( "t" )
		);
		assertThat(
				func.getPluralAttributeBinding().getAttribute().getAttributeName(),
				is( "basicCollection" )
		);
	}

	@Test
	public void testCollectionIndexFunction() {
		SqmSelectStatement statement = interpret( "SELECT l.basicName FROM Trip t JOIN t.indexedCollectionLegs l WHERE INDEX( l ) > 2" );

		SqmPredicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate relationalPredicate = ( (RelationalSqmPredicate) predicate );

		assertThat( relationalPredicate.getOperator(), is( RelationalSqmPredicate.Operator.GREATER_THAN ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerSqmExpression.class ) );
		assertThat( ( (LiteralIntegerSqmExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 2 ) );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( CollectionIndexSqmExpression.class ) );
		assertThat( ( (CollectionIndexSqmExpression) relationalPredicate.getLeftHandExpression() ).getAttributeBinding().getFromElement().getIdentificationVariable(), is( "l" ) );
	}

	@Test
	public void testMapKeyFunction() {
		SqmSelectStatement statement = interpret( "SELECT l.basicName FROM Trip t JOIN t.mapLegs l WHERE KEY( l ) = 'foo'" );

		SqmPredicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalSqmPredicate.class ) );
		RelationalSqmPredicate relationalPredicate = ( (RelationalSqmPredicate) predicate );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( MapKeyBinding.class ) );
		final MapKeyBinding mapKeyExpression = (MapKeyBinding) relationalPredicate.getLeftHandExpression();
		final PluralAttributeReference attributeReference = (PluralAttributeReference) mapKeyExpression.getPluralAttributeBinding().getAttribute();

		assertThat( attributeReference.getIndexReference().getClassification(), is( IndexClassification.BASIC) );
		assertThat( attributeReference.getIndexReference().getType(), instanceOf( BasicType.class ) );
		assertEquals( String.class, ( (BasicTypeImpl) attributeReference.getIndexReference().getType() ).getJavaType() );

		assertThat( mapKeyExpression.getPluralAttributeBinding().getFromElement().getIdentificationVariable(), is( "l" ) );
	}

	private SqmSelectStatement interpret(String query) {
		return (SqmSelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
	}


	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl legType = metamodel.makeEntityType( "com.acme.Leg" );
		legType.makeSingularAttribute(
				"basicName",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		EntityTypeImpl tripType = metamodel.makeEntityType( "com.acme.Trip" );

		tripType.makeSetAttribute(
				"basicCollection",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		tripType.makeListAttribute(
				"indexedCollectionLegs",
				StandardBasicTypeDescriptors.INSTANCE.INTEGER,
				legType
		);
		tripType.makeMapAttribute(
				"mapLegs",
				StandardBasicTypeDescriptors.INSTANCE.STRING,
				legType
		);

		return metamodel;
	}
}
