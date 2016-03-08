/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.CollectionIndexFunction;
import org.hibernate.sqm.query.expression.CollectionSizeFunction;
import org.hibernate.sqm.query.expression.LiteralIntegerExpression;
import org.hibernate.sqm.query.expression.MapKeyPathExpression;
import org.hibernate.sqm.query.predicate.NullnessPredicate;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
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
		SelectStatement statement = interpret( "select l from Leg l where l.basicName is not null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessPredicate.class ) );
		NullnessPredicate predicate = (NullnessPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(true) );
	}

	@Test
	public void testNotIsNullPredicate() {
		SelectStatement statement = interpret( "select l from Leg l where not l.basicName is null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessPredicate.class ) );
		NullnessPredicate predicate = (NullnessPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(true) );
	}

	@Test
	public void testNotIsNotNullPredicate() {
		SelectStatement statement = interpret( "select l from Leg l where not l.basicName is not null" );
		assertThat( statement.getQuerySpec().getWhereClause().getPredicate(), instanceOf( NullnessPredicate.class ) );
		NullnessPredicate predicate = (NullnessPredicate) statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate.isNegated(), is(false) );
	}

	@Test
	public void testCollectionSizeFunction() {
		SelectStatement statement = interpret( "SELECT t FROM Trip t WHERE SIZE( t.basicCollection ) = 311" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getType(), is( RelationalPredicate.Type.EQUAL ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerExpression.class ) );
		assertThat( ( (LiteralIntegerExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 311 ) );

		assertThat(
				relationalPredicate.getLeftHandExpression(),
				instanceOf( CollectionSizeFunction.class )
		);
		final CollectionSizeFunction func = (CollectionSizeFunction) relationalPredicate.getLeftHandExpression();
		assertThat(
				func.getPluralAttributeBinding().getAttributeBindingSource().getFromElement().getIdentificationVariable(),
				is( "t" )
		);
		assertThat(
				func.getPluralAttributeBinding().getBoundAttribute().getName(),
				is( "basicCollection" )
		);
	}

	@Test
	public void testCollectionIndexFunction() {
		SelectStatement statement = interpret( "SELECT l.basicName FROM Trip t JOIN t.indexedCollectionLegs l WHERE INDEX( l ) > 2" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getType(), is( RelationalPredicate.Type.GT ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerExpression.class ) );
		assertThat( ( (LiteralIntegerExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 2 ) );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( CollectionIndexFunction.class ) );
		assertThat( ( (CollectionIndexFunction) relationalPredicate.getLeftHandExpression() ).getCollectionAlias(), is( "l" ) );
	}

	@Test
	public void testMapKeyFunction() {
		SelectStatement statement = interpret( "SELECT l.basicName FROM Trip t JOIN t.mapLegs l WHERE KEY( l ) = 'foo'" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( MapKeyPathExpression.class ) );
		assertThat( ( (MapKeyPathExpression) relationalPredicate.getLeftHandExpression() ).getCollectionAlias(), is( "l" ) );
		assertThat( ( (MapKeyPathExpression) relationalPredicate.getLeftHandExpression() ).getMapKeyType().getTypeName(), is( "java.lang.String" ) );
	}

	private SelectStatement interpret(String query) {
		return (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
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
