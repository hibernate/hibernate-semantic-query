/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.CollectionIndexFunction;
import org.hibernate.sqm.query.expression.CollectionSizeFunction;
import org.hibernate.sqm.query.expression.CollectionValueFunction;
import org.hibernate.sqm.query.expression.EntityTypeExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerExpression;
import org.hibernate.sqm.query.expression.MapKeyFunction;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.TypeFunction;
import org.hibernate.sqm.query.predicate.InTupleListPredicate;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Tests for elements of WHERE clauses.
 *
 * @author Gunnar Morling
 */
public class WhereClauseTests {

	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl();

	@Test
	public void testCollectionSizeFunction() {
		SelectStatement statement = interpret( "SELECT t FROM Trip t WHERE SIZE( t.basicCollection ) = 311" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getType(), is( RelationalPredicate.Type.EQUAL ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( LiteralIntegerExpression.class ) );
		assertThat( ( (LiteralIntegerExpression) relationalPredicate.getRightHandExpression() ).getLiteralValue(), is( 311 ) );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( CollectionSizeFunction.class ) );
		assertThat( ( (CollectionSizeFunction) relationalPredicate.getLeftHandExpression() ).getFromElementAlias(), is( "t" ) );
		assertThat( ( (CollectionSizeFunction) relationalPredicate.getLeftHandExpression() ).getAttributeDescriptor().getName(), is( "basicCollection" ) );
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

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( MapKeyFunction.class ) );
		assertThat( ( (MapKeyFunction) relationalPredicate.getLeftHandExpression() ).getCollectionAlias(), is( "l" ) );
		assertThat( ( (MapKeyFunction) relationalPredicate.getLeftHandExpression() ).getMapKeyType().getTypeName(), is( "com.acme.map-key:mapLegs" ) );
	}

	@Test
	public void testTypeFunction() {
		SelectStatement statement = interpret( "SELECT a FROM Animal a WHERE TYPE( a ) = Mammal" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( TypeFunction.class ) );
		assertThat(
				relationalPredicate.getLeftHandExpression().getTypeDescriptor(),
				sameInstance( (TypeDescriptor) StandardBasicTypeDescriptors.INSTANCE.CLASS )
		);
		assertThat( ( (TypeFunction) relationalPredicate.getLeftHandExpression() ).getFromElementAlias(), is( "a" ) );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( EntityTypeExpression.class ) );
		assertThat(
				relationalPredicate.getRightHandExpression().getTypeDescriptor(),
				sameInstance( (TypeDescriptor) StandardBasicTypeDescriptors.INSTANCE.CLASS )
		);

		assertThat(
				( (EntityTypeExpression) relationalPredicate.getRightHandExpression() ).getEntityTypeDescriptor().getTypeName(),
				is( "com.acme.Mammal" )
		);
	}

	@Test
	public void testTypeFunctionWithNamedParameter() {
		SelectStatement statement = interpret( "SELECT a FROM Animal a WHERE TYPE( a ) = TYPE( :searchedType )" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		// TYPE( a )
		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( TypeFunction.class ) );
		assertThat(
				relationalPredicate.getLeftHandExpression().getTypeDescriptor(),
				sameInstance( (TypeDescriptor) StandardBasicTypeDescriptors.INSTANCE.CLASS )
		);
		assertThat( ( (TypeFunction) relationalPredicate.getLeftHandExpression() ).getFromElementAlias(), is( "a" ) );

		// TYPE( :searchedType )
		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( TypeFunction.class ) );
		assertThat(
				relationalPredicate.getRightHandExpression().getTypeDescriptor(),
				sameInstance( (TypeDescriptor) StandardBasicTypeDescriptors.INSTANCE.CLASS )
		);

		assertThat(
				( (TypeFunction) relationalPredicate.getRightHandExpression() ).getExpression(),
				instanceOf( NamedParameterExpression.class )
		);
		NamedParameterExpression positionalParameterExpression = (NamedParameterExpression) ( (TypeFunction) relationalPredicate
				.getRightHandExpression() ).getExpression();
		assertThat( positionalParameterExpression.getName(), is( "searchedType" ) );
	}

	@Test
	public void testTypeFunctionWithPositionalParameter() {
		SelectStatement statement = interpret( "SELECT a FROM Animal a WHERE TYPE( a ) = TYPE( ?1 )" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getRightHandExpression(), instanceOf( TypeFunction.class ) );
		assertThat(
				relationalPredicate.getRightHandExpression().getTypeDescriptor(),
				sameInstance( (TypeDescriptor) StandardBasicTypeDescriptors.INSTANCE.CLASS )
		);
		assertThat(
				( (TypeFunction) relationalPredicate.getRightHandExpression() ).getExpression(),
				instanceOf( PositionalParameterExpression.class )
		);
		PositionalParameterExpression positionalParameterExpression = (PositionalParameterExpression) ( (TypeFunction) relationalPredicate
				.getRightHandExpression() ).getExpression();
		assertThat( positionalParameterExpression.getPosition(), is( 1 ) );
	}

	@Test
	public void testTypeFunctionWithParametersInList() {
		SelectStatement statement = interpret( "SELECT a FROM Animal a WHERE TYPE( a ) IN (:animalType1, :animalType2)" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( InTupleListPredicate.class ) );
		InTupleListPredicate inList = ( (InTupleListPredicate) predicate );

		assertThat( inList.getTestExpression(), instanceOf( TypeFunction.class ) );
		assertThat( inList.getTupleListExpressions().size(), is( 2 ) );
		assertThat(
				inList.getTupleListExpressions().get( 0 ),
				instanceOf( NamedParameterExpression.class )
		);
		assertThat(
				inList.getTupleListExpressions().get( 1 ),
				instanceOf( NamedParameterExpression.class )
		);
	}

	@Test
	public void testTypeFunctionWithCollection() {
		SelectStatement statement = interpret( "SELECT l.basicName FROM Trip t JOIN t.collectionLegs l WHERE TYPE( l ) = ExtendedLeg" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( TypeFunction.class ) );

		assertThat(
				( (TypeFunction) relationalPredicate.getLeftHandExpression() ).getFromElementAlias(),
				is( "l" )
		);
	}

	@Test
	public void testTypeFunctionWithSimpleProperty() {
		SelectStatement statement = interpret( "SELECT t.basicName FROM Trip t WHERE TYPE( t.basicLength ) = Integer" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( TypeFunction.class ) );
		TypeFunction typeFunction = (TypeFunction) relationalPredicate.getLeftHandExpression();

		assertThat( typeFunction.getFromElementAlias(), is( "t" ) );
		assertThat( typeFunction.getAttributeDescriptor().getName(), is( "basicLength" ) );
	}

	@Test
	public void testTypeFunctionWithMapKeyFunction() {
		SelectStatement statement = interpret( "SELECT l.basicName FROM Trip t JOIN t.mapLegs l WHERE TYPE( KEY( l ) ) = ExtendedLeg" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( TypeFunction.class ) );

		assertThat(
				( (TypeFunction) relationalPredicate.getLeftHandExpression() ).getExpression(),
				instanceOf( MapKeyFunction.class )
		);
	}

	@Test
	public void testTypeFunctionWithValueFunction() {
		SelectStatement statement = interpret( "SELECT l.basicName FROM Trip t JOIN t.mapLegs l WHERE TYPE( VALUE( l ) ) = ExtendedLeg" );

		Predicate predicate = statement.getQuerySpec().getWhereClause().getPredicate();
		assertThat( predicate, instanceOf( RelationalPredicate.class ) );
		RelationalPredicate relationalPredicate = ( (RelationalPredicate) predicate );

		assertThat( relationalPredicate.getLeftHandExpression(), instanceOf( TypeFunction.class ) );

		assertThat(
				( (TypeFunction) relationalPredicate.getLeftHandExpression() ).getExpression(),
				instanceOf( CollectionValueFunction.class )
		);
	}

	private SelectStatement interpret(String query) {
		return (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
	}
}
