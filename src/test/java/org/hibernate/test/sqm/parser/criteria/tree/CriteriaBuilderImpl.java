/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.hibernate.orm.type.internal.BasicTypeImpl;
import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.parser.criteria.tree.JpaCriteriaQuery;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.parser.criteria.tree.JpaOrder;
import org.hibernate.sqm.parser.criteria.tree.JpaPredicate;
import org.hibernate.sqm.parser.criteria.tree.select.JpaCompoundSelection;
import org.hibernate.sqm.parser.criteria.tree.select.JpaSelection;
import org.hibernate.sqm.query.predicate.RelationalPredicateOperator;

import org.hibernate.test.sqm.parser.criteria.tree.expression.LiteralExpression;
import org.hibernate.test.sqm.parser.criteria.tree.expression.ParameterExpressionImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.function.GenericFunctionExpression;
import org.hibernate.test.sqm.parser.criteria.tree.predicate.BooleanExpressionPredicate;
import org.hibernate.test.sqm.parser.criteria.tree.predicate.ComparisonPredicate;
import org.hibernate.test.sqm.parser.criteria.tree.predicate.CompoundPredicate;
import org.hibernate.test.sqm.parser.criteria.tree.predicate.NullnessPredicate;
import org.hibernate.test.sqm.parser.criteria.tree.select.ArrayJpaSelectionImpl;
import org.hibernate.test.sqm.parser.criteria.tree.select.DynamicInstantiationImpl;
import org.hibernate.test.sqm.parser.criteria.tree.select.TupleJpaSelectionImpl;


/**
 * Hibernate implementation of the JPA {@link CriteriaBuilder} contract.
 *
 * @author Steve Ebersole
 */
public class CriteriaBuilderImpl implements CriteriaBuilder, Serializable {
	private final ConsumerContext consumerContext;

	public CriteriaBuilderImpl(ConsumerContext consumerContext) {
		this.consumerContext = consumerContext;
	}

	public ConsumerContext consumerContext() {
		return consumerContext;
	}


	// Query builders ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public JpaCriteriaQuery<Object> createQuery() {
		return new CriteriaQueryImpl<>( this, Object.class );
	}

	@Override
	public <T> JpaCriteriaQuery<T> createQuery(Class<T> resultClass) {
		return new CriteriaQueryImpl<T>( this, resultClass );
	}

	@Override
	public JpaCriteriaQuery<Tuple> createTupleQuery() {
		return new CriteriaQueryImpl<Tuple>( this, Tuple.class );
	}

	@Override
	public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
//		return new CriteriaUpdateImpl<T>( this );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
//		return new CriteriaDeleteImpl<T>( this );
		throw new NotYetImplementedException(  );
	}


	// selections ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Package-protected method to centralize checking of criteria query multi-selects as defined by the
	 * {@link CriteriaQuery#multiselect(List)}  method.
	 *
	 * @param selections The selection varargs to check
	 *
	 * @throws IllegalArgumentException If the selection items are not valid per {@link CriteriaQuery#multiselect}
	 * documentation.
	 * <i>&quot;An argument to the multiselect method must not be a tuple-
     * or array-valued compound selection item.&quot;</i>
	 */
	void checkMultiselect(List<Selection<?>> selections) {
//		final HashSet<String> aliases = new HashSet<String>( CollectionHelper.determineProperSizing( selections.size() ) );
//
//		for ( Selection<?> selection : selections ) {
//			if ( selection.isCompoundSelection() ) {
//				if ( selection.getJavaType().isArray() ) {
//					throw new IllegalArgumentException(
//							"Selection items in a multi-select cannot contain compound array-valued elements"
//					);
//				}
//				if ( Tuple.class.isAssignableFrom( selection.getJavaType() ) ) {
//					throw new IllegalArgumentException(
//							"Selection items in a multi-select cannot contain compound tuple-valued elements"
//					);
//				}
//			}
//			if ( StringHelper.isNotEmpty( selection.getAlias() ) ) {
//				boolean added = aliases.add( selection.getAlias() );
//				if ( ! added ) {
//					throw new IllegalArgumentException( "Multi-select expressions defined duplicate alias : " + selection.getAlias() );
//				}
//			}
//		}
	}

	@Override
	public JpaCompoundSelection<Tuple> tuple(Selection<?>... selections) {
		return tuple( Arrays.asList( selections ) );
	}

	/**
	 * Version of {@link #tuple(Selection[])} taking a list.
	 *
	 * @param selections List of selections.
	 *
	 * @return The tuple compound selection
	 */
	public JpaCompoundSelection<Tuple> tuple(List<Selection<?>> selections) {
		checkMultiselect( selections );
		return new TupleJpaSelectionImpl( this, Tuple.class, toExpressions( selections ) );
	}

	@Override
	public JpaCompoundSelection<Object[]> array(Selection<?>... selections) {
		return array( Arrays.asList( selections ) );
	}

	/**
	 * Version of {@link #array(Selection[])} taking a list of selections.
	 *
	 * @param selections List of selections.
	 *
	 * @return The array compound selection
	 */
	public JpaCompoundSelection<Object[]> array(List<Selection<?>> selections) {
		return array( Object[].class, selections );
	}

	/**
	 * Version of {@link #array(Selection[])} taking a list of selections,
	 * as well as the type of array.
	 *
	 * @param type The type of array
	 * @param selections List of selections.
	 *
	 * @return The array compound selection
	 */
	public <Y> JpaCompoundSelection<Y> array(Class<Y> type, List<Selection<?>> selections) {
		checkMultiselect( selections );
		return new ArrayJpaSelectionImpl<>( this, type, toExpressions( selections ) );
	}

	@Override
	public <Y> JpaCompoundSelection<Y> construct(Class<Y> result, Selection<?>... selections) {
		return construct( result, Arrays.asList( selections ) );
	}

	/**
	 * Version of {@link #construct(Class,Selection[])} taking the
	 * to-be-constructed type as well as a list of selections.
	 *
	 * @param result The result class to be constructed.
	 * @param selections The selections to use in the constructor call.
	 *
	 * @return The <b>view</b> compound selection.
	 */
	public <Y> JpaCompoundSelection<Y> construct(Class<Y> result, List<Selection<?>> selections) {
		checkMultiselect( selections );
		return new DynamicInstantiationImpl<>( this, result, toExpressions( selections ) );
	}

	private List<JpaExpression<?>> toExpressions(List<Selection<?>> selections) {
		if ( selections == null || selections.isEmpty() ) {
			return Collections.emptyList();
		}

		final ArrayList<JpaExpression<?>> expressions = new ArrayList<>();
		for ( Selection<?> selection : selections ) {
			if ( selection instanceof JpaExpression ) {
				expressions.add( (JpaExpression) selection );
			}
			else {
				throw new CriteriaBuilderException(
						"Expecting javax.persistence.criteria.Selection to be " +
								"org.hibernate.sqm.parser.criteria.tree.JpaExpression, but found " +
								selection.toString()
				);
			}
		}

		return expressions;
	}


	// ordering ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public JpaOrder asc(Expression<?> x) {
		if ( !JpaExpression.class.isInstance( x ) ) {
			throw new CriteriaBuilderException( "Expression no an instance of JpaExpression" );
		}
		return new OrderImpl( (JpaExpression<?>) x, true );
	}

	@Override
	public JpaOrder desc(Expression<?> x) {
		if ( !JpaExpression.class.isInstance( x ) ) {
			throw new CriteriaBuilderException( "Expression no an instance of JpaExpression" );
		}
		return new OrderImpl( (JpaExpression<?>) x, false );
	}


	// predicates ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public JpaPredicate wrap(Expression<Boolean> expression) {
		if ( JpaPredicate.class.isInstance( expression ) ) {
			return ( (JpaPredicate) expression );
		}
		else {
			return new BooleanExpressionPredicate( this, (JpaExpression<Boolean>) expression );
		}
	}

	@Override
	public JpaPredicate not(Expression<Boolean> expression) {
		return wrap( expression ).not();
	}

	@Override
	@SuppressWarnings("unchecked")
	public JpaPredicate and(Expression<Boolean> x, Expression<Boolean> y) {
		return new CompoundPredicate( this, Predicate.BooleanOperator.AND, x, y );
	}

	@Override
	@SuppressWarnings("unchecked")
	public JpaPredicate or(Expression<Boolean> x, Expression<Boolean> y) {
		return new CompoundPredicate( this, Predicate.BooleanOperator.OR, x, y );
	}

	@Override
	public JpaPredicate and(Predicate... restrictions) {
		return new CompoundPredicate( this, Predicate.BooleanOperator.AND, restrictions );
	}

	@Override
	public JpaPredicate or(Predicate... restrictions) {
		return new CompoundPredicate( this, Predicate.BooleanOperator.OR, restrictions );
	}

	@Override
	public JpaPredicate conjunction() {
		return new CompoundPredicate( this, Predicate.BooleanOperator.AND );
	}

	@Override
	public JpaPredicate disjunction() {
		return new CompoundPredicate( this, Predicate.BooleanOperator.OR );
	}

	@Override
	public JpaPredicate isTrue(Expression<Boolean> expression) {
		if ( JpaPredicate.class.isInstance( expression ) ) {
			return (JpaPredicate) expression;
		}

		return wrap( expression );
	}

	@Override
	public JpaPredicate isFalse(Expression<Boolean> expression) {
		if ( JpaPredicate.class.isInstance( expression ) ) {
			final JpaPredicate predicate = (JpaPredicate) expression;
			predicate.not();
			return predicate;
		}

		return wrap( expression ).not();
	}

	@Override
	public JpaPredicate isNull(Expression<?> x) {
		check( x );
		return new NullnessPredicate( this, (JpaExpression<?>) x );
	}

	@Override
	public JpaPredicate isNotNull(Expression<?> x) {
		return isNull( x ).not();
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public JpaPredicate equal(Expression<?> x, Expression<?> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.EQUAL,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate notEqual(Expression<?> x, Expression<?> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.NOT_EQUAL,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate equal(Expression<?> x, Object y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.EQUAL,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate notEqual(Expression<?> x, Object y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.NOT_EQUAL,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Expression<? extends Y> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.GREATER_THAN,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public <Y extends Comparable<? super Y>> Predicate lessThan(
			Expression<? extends Y> x,
			Expression<? extends Y> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.LESS_THAN,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(
			Expression<? extends Y> x,
			Expression<? extends Y> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.GREATER_THAN_OR_EQUAL,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(
			Expression<? extends Y> x,
			Expression<? extends Y> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.LESS_THAN_OR_EQUAL,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public <Y extends Comparable<? super Y>> Predicate greaterThan(
			Expression<? extends Y> x,
			Y y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.GREATER_THAN,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public <Y extends Comparable<? super Y>> Predicate lessThan(
			Expression<? extends Y> x,
			Y y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.LESS_THAN,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(
			Expression<? extends Y> x,
			Y y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.GREATER_THAN_OR_EQUAL,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public<Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(
			Expression<? extends Y> x,
			Y y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.LESS_THAN_OR_EQUAL,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate gt(Expression<? extends Number> x, Expression<? extends Number> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.GREATER_THAN,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate lt(Expression<? extends Number> x, Expression<? extends Number> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.LESS_THAN,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate ge(Expression<? extends Number> x, Expression<? extends Number> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.GREATER_THAN_OR_EQUAL,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate le(Expression<? extends Number> x, Expression<? extends Number> y) {
		check( x );
		check( y );
		return new ComparisonPredicate(
				this, RelationalPredicateOperator.LESS_THAN_OR_EQUAL,
				(JpaExpression<?>) x,
				(JpaExpression<?>) y
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate gt(Expression<? extends Number> x, Number y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.GREATER_THAN,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate lt(Expression<? extends Number> x, Number y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.LESS_THAN,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate ge(Expression<? extends Number> x, Number y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.GREATER_THAN_OR_EQUAL,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	public Predicate le(Expression<? extends Number> x, Number y) {
		check( x );
		return new ComparisonPredicate(
				this,
				RelationalPredicateOperator.LESS_THAN_OR_EQUAL,
				(JpaExpression<?>) x,
				literal( y )
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate between(
			Expression<? extends Y> expression,
			Y lowerBound,
			Y upperBound) {
//		return new BetweenPredicate<Y>( this, expression, lowerBound, upperBound );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate between(
			Expression<? extends Y> expression,
			Expression<? extends Y> lowerBound,
			Expression<? extends Y> upperBound) {
//		return new BetweenPredicate<Y>( this, expression, lowerBound, upperBound );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <T> In<T> in(Expression<? extends T> expression) {
//		return new InPredicate<T>( this, expression );
		throw new NotYetImplementedException(  );
	}

	public <T> In<T> in(Expression<? extends T> expression, Expression<? extends T>... values) {
//		return new InPredicate<T>( this, expression, values );
		throw new NotYetImplementedException(  );
	}

	public <T> In<T> in(Expression<? extends T> expression, T... values) {
//		return new InPredicate<T>( this, expression, values );
		throw new NotYetImplementedException(  );
	}

	public <T> In<T> in(Expression<? extends T> expression, Collection<T> values) {
//		return new InPredicate<T>( this, expression, values );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Predicate like(Expression<String> matchExpression, Expression<String> pattern) {
//		return new LikePredicate( this, matchExpression, pattern );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Predicate like(Expression<String> matchExpression, Expression<String> pattern, Expression<Character> escapeCharacter) {
//		return new LikePredicate( this, matchExpression, pattern, escapeCharacter );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Predicate like(Expression<String> matchExpression, Expression<String> pattern, char escapeCharacter) {
//		return new LikePredicate( this, matchExpression, pattern, escapeCharacter );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Predicate like(Expression<String> matchExpression, String pattern) {
//		return new LikePredicate( this, matchExpression, pattern );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Predicate like(Expression<String> matchExpression, String pattern, Expression<Character> escapeCharacter) {
//		return new LikePredicate( this, matchExpression, pattern, escapeCharacter );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Predicate like(Expression<String> matchExpression, String pattern, char escapeCharacter) {
//		return new LikePredicate( this, matchExpression, pattern, escapeCharacter );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Predicate notLike(Expression<String> matchExpression, Expression<String> pattern) {
		return like( matchExpression, pattern ).not();
	}

	@Override
	public Predicate notLike(Expression<String> matchExpression, Expression<String> pattern, Expression<Character> escapeCharacter) {
		return like( matchExpression, pattern, escapeCharacter ).not();
	}

	@Override
	public Predicate notLike(Expression<String> matchExpression, Expression<String> pattern, char escapeCharacter) {
		return like( matchExpression, pattern, escapeCharacter ).not();
	}

	@Override
	public Predicate notLike(Expression<String> matchExpression, String pattern) {
		return like( matchExpression, pattern ).not();
	}

	@Override
	public Predicate notLike(Expression<String> matchExpression, String pattern, Expression<Character> escapeCharacter) {
		return like( matchExpression, pattern, escapeCharacter ).not();
	}

	@Override
	public Predicate notLike(Expression<String> matchExpression, String pattern, char escapeCharacter) {
		return like( matchExpression, pattern, escapeCharacter ).not();
	}


	// parameters ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public <T> ParameterExpression<T> parameter(Class<T> paramClass) {
		final BasicTypeImpl sqmType = (BasicTypeImpl) consumerContext().getDomainMetamodel().resolveBasicType( paramClass );
		return new ParameterExpressionImpl<T>( this, sqmType, paramClass );
	}

	@Override
	public <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
		final BasicTypeImpl sqmType = (BasicTypeImpl) consumerContext().getDomainMetamodel().resolveBasicType( paramClass );
		return new ParameterExpressionImpl<T>( this, sqmType, paramClass, name );
	}

	@Override
	public <T> Expression<T> literal(T value) {
		if ( value == null ) {
			throw new IllegalArgumentException( "literal value cannot be null" );
		}
		return new LiteralExpression<T>( this, value );
	}

	@Override
	public <T> Expression<T> nullLiteral(Class<T> resultClass) {
//		return new NullLiteralExpression<T>( this, resultClass );
		throw new NotYetImplementedException(  );
	}


	// aggregate functions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public <N extends Number> Expression<Double> avg(Expression<N> x) {
//		return new AggregationFunction.AVG( this, x );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <N extends Number> Expression<N> sum(Expression<N> x) {
//		return new AggregationFunction.SUM<N>( this, x );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Long> sumAsLong(Expression<Integer> x) {
//		return new AggregationFunction.SUM<Long>( this, x, Long.class );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Double> sumAsDouble(Expression<Float> x) {
//		return new AggregationFunction.SUM<Double>( this, x, Double.class );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <N extends Number> Expression<N> max(Expression<N> x) {
//		return new AggregationFunction.MAX<N>( this, x );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <N extends Number> Expression<N> min(Expression<N> x) {
//		return new AggregationFunction.MIN<N>( this, x );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x) {
//		return new AggregationFunction.GREATEST( this, x );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <X extends Comparable<? super X>> Expression<X> least(Expression<X> x) {
//		return new AggregationFunction.LEAST( this, x );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Long> count(Expression<?> x) {
//		return new AggregationFunction.COUNT( this, x, false );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Long> countDistinct(Expression<?> x) {
//		return new AggregationFunction.COUNT( this, x, true );
		throw new NotYetImplementedException(  );
	}


	// other functions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	@SuppressWarnings("unchecked")
	public <T> Expression<T> function(String name, Class<T> returnType, Expression<?>... arguments) {
		if ( arguments == null || arguments.length == 0 ) {
			return function( name, returnType );
		}

		final SqmExpressableTypeBasic returnSqmType = consumerContext().getDomainMetamodel().resolveBasicType( returnType );
		return new GenericFunctionExpression(
				name,
				returnSqmType,
				returnType,
				this,
				(JpaExpression<?>[]) arguments
		);
	}

	/**
	 * Create a reference to a function taking no params.
	 *
	 * @param name The function name.
	 * @param returnType The return type.
	 *
	 * @return The function expression
	 */
	@SuppressWarnings("unchecked")
	public <T> Expression<T> function(String name, Class<T> returnType) {
		final SqmExpressableTypeBasic returnSqmType = consumerContext().getDomainMetamodel().resolveBasicType( returnType );
		return new GenericFunctionExpression<>( name, returnSqmType, returnType, this );
	}

	@Override
	public <N extends Number> Expression<N> abs(Expression<N> expression) {
//		return new AbsFunction<N>( this, expression );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Double> sqrt(Expression<? extends Number> expression) {
//		return new SqrtFunction( this, expression );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<java.sql.Date> currentDate() {
//		return new CurrentDateFunction( this );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<java.sql.Timestamp> currentTimestamp() {
//		return new CurrentTimestampFunction( this );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<java.sql.Time> currentTime() {
//		return new CurrentTimeFunction( this );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> substring(Expression<String> value, Expression<Integer> start) {
//		return new SubstringFunction( this, value, start );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> substring(Expression<String> value, int start) {
//		return new SubstringFunction( this, value, start );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> substring(Expression<String> value, Expression<Integer> start, Expression<Integer> length) {
//		return new SubstringFunction( this, value, start, length );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> substring(Expression<String> value, int start, int length) {
//		return new SubstringFunction( this, value, start, length );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> trim(Expression<String> trimSource ) {
//		return new TrimFunction( this, trimSource );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> trim(Trimspec trimspec, Expression<String> trimSource) {
//		return new TrimFunction( this, trimspec, trimSource );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> trim(Expression<Character> trimCharacter, Expression<String> trimSource) {
//		return new TrimFunction( this, trimCharacter, trimSource );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> trim(Trimspec trimspec, Expression<Character> trimCharacter, Expression<String> trimSource) {
//		return new TrimFunction( this, trimspec, trimCharacter, trimSource );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> trim(char trimCharacter, Expression<String> trimSource) {
//		return new TrimFunction( this, trimCharacter, trimSource );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> trim(Trimspec trimspec, char trimCharacter, Expression<String> trimSource) {
//		return new TrimFunction( this, trimspec, trimCharacter, trimSource );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> lower(Expression<String> value) {
//		return new LowerFunction( this, value );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> upper(Expression<String> value) {
//		return new UpperFunction( this, value );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Integer> length(Expression<String> value) {
//		return new LengthFunction( this, value );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Integer> locate(Expression<String> string, Expression<String> pattern) {
//		return new LocateFunction( this, pattern, string );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Integer> locate(Expression<String> string, Expression<String> pattern, Expression<Integer> start) {
//		return new LocateFunction( this, pattern, string, start );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Integer> locate(Expression<String> string, String pattern) {
//		return new LocateFunction( this, pattern, string );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Integer> locate(Expression<String> string, String pattern, int start) {
//		return new LocateFunction( this, pattern, string, start );
		throw new NotYetImplementedException(  );
	}


	// arithmetic operations ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public <N extends Number> Expression<N> neg(Expression<N> expression) {
//		return new UnaryArithmeticOperation<N>(
//				this,
//				UnaryArithmeticOperation.Operation.UNARY_MINUS,
//				expression
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> sum(Expression<? extends N> expression1, Expression<? extends N> expression2) {
//		if ( expression1 == null || expression2 == null ) {
//			throw new IllegalArgumentException( "arguments to sum() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( expression1.getJavaType(), expression2.getJavaType() );
//
//		return new BinaryArithmeticOperation<N>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.ADD,
//				expression1,
//				expression2
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> prod(Expression<? extends N> expression1, Expression<? extends N> expression2) {
//		if ( expression1 == null || expression2 == null ) {
//			throw new IllegalArgumentException( "arguments to prod() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( expression1.getJavaType(), expression2.getJavaType() );
//
//		return new BinaryArithmeticOperation<N>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.MULTIPLY,
//				expression1,
//				expression2
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> diff(Expression<? extends N> expression1, Expression<? extends N> expression2) {
//		if ( expression1 == null || expression2 == null ) {
//			throw new IllegalArgumentException( "arguments to diff() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( expression1.getJavaType(), expression2.getJavaType() );
//
//		return new BinaryArithmeticOperation<N>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.SUBTRACT,
//				expression1,
//				expression2
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> sum(Expression<? extends N> expression, N n) {
//		if ( expression == null || n == null ) {
//			throw new IllegalArgumentException( "arguments to sum() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( expression.getJavaType(), n.getClass() );
//
//		return new BinaryArithmeticOperation<N>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.ADD,
//				expression,
//				n
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> prod(Expression<? extends N> expression, N n) {
//		if ( expression == null || n == null ) {
//			throw new IllegalArgumentException( "arguments to prod() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( expression.getJavaType(), n.getClass() );
//
//		return new BinaryArithmeticOperation<N>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.MULTIPLY,
//				expression,
//				n
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> diff(Expression<? extends N> expression, N n) {
//		if ( expression == null || n == null ) {
//			throw new IllegalArgumentException( "arguments to diff() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( expression.getJavaType(), n.getClass() );
//
//		return new BinaryArithmeticOperation<N>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.SUBTRACT,
//				expression,
//				n
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> sum(N n, Expression<? extends N> expression) {
//		if ( expression == null || n == null ) {
//			throw new IllegalArgumentException( "arguments to sum() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( n.getClass(), expression.getJavaType() );
//
//		return new BinaryArithmeticOperation<N>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.ADD,
//				n,
//				expression
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> prod(N n, Expression<? extends N> expression) {
//		if ( n == null || expression == null ) {
//			throw new IllegalArgumentException( "arguments to prod() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( n.getClass(), expression.getJavaType() );
//
//		return (BinaryArithmeticOperation<N>) new BinaryArithmeticOperation(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.MULTIPLY,
//				n,
//				expression
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <N extends Number> Expression<N> diff(N n, Expression<? extends N> expression) {
//		if ( n == null || expression == null ) {
//			throw new IllegalArgumentException( "arguments to diff() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( n.getClass(), expression.getJavaType() );
//
//		return new BinaryArithmeticOperation<N>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.SUBTRACT,
//				n,
//				expression
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings( {"unchecked"})
	public Expression<Number> quot(Expression<? extends Number> expression1, Expression<? extends Number> expression2) {
//		if ( expression1 == null || expression2 == null ) {
//			throw new IllegalArgumentException( "arguments to quot() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( expression1.getJavaType(), expression2.getJavaType(), true );
//
//		return new BinaryArithmeticOperation<Number>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.DIVIDE,
//				expression1,
//				expression2
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings( {"unchecked"})
	public Expression<Number> quot(Expression<? extends Number> expression, Number number) {
//		if ( expression == null || number == null ) {
//			throw new IllegalArgumentException( "arguments to quot() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( expression.getJavaType(), number.getClass(), true );
//
//		return new BinaryArithmeticOperation<Number>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.DIVIDE,
//				expression,
//				number
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings( {"unchecked"})
	public Expression<Number> quot(Number number, Expression<? extends Number> expression) {
//		if ( expression == null || number == null ) {
//			throw new IllegalArgumentException( "arguments to quot() cannot be null" );
//		}
//
//		final Class resultType = BinaryArithmeticOperation.determineResultType( number.getClass(), expression.getJavaType(), true );
//
//		return new BinaryArithmeticOperation<Number>(
//				this,
//				resultType,
//				BinaryArithmeticOperation.Operation.DIVIDE,
//				number,
//				expression
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Integer> mod(Expression<Integer> expression1, Expression<Integer> expression2) {
//		if ( expression1 == null || expression2 == null ) {
//			throw new IllegalArgumentException( "arguments to mod() cannot be null" );
//		}
//
//		return new BinaryArithmeticOperation<Integer>(
//				this,
//				Integer.class,
//				BinaryArithmeticOperation.Operation.MOD,
//				expression1,
//				expression2
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Integer> mod(Expression<Integer> expression, Integer integer) {
//		if ( expression == null || integer == null ) {
//			throw new IllegalArgumentException( "arguments to mod() cannot be null" );
//		}
//
//		return new BinaryArithmeticOperation<Integer>(
//				this,
//				Integer.class,
//				BinaryArithmeticOperation.Operation.MOD,
//				expression,
//				integer
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<Integer> mod(Integer integer, Expression<Integer> expression) {
//		if ( integer == null || expression == null ) {
//			throw new IllegalArgumentException( "arguments to mod() cannot be null" );
//		}
//
//		return new BinaryArithmeticOperation<Integer>(
//				this,
//				Integer.class,
//				BinaryArithmeticOperation.Operation.MOD,
//				integer,
//				expression
//		);
		throw new NotYetImplementedException(  );
	}


	// casting ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


	@Override
	public JpaExpression<Long> toLong(Expression<? extends Number> expression) {
		check( expression );
		return ( (JpaExpression<?>) expression ).asLong();
	}

	@Override
	public JpaExpression<Integer> toInteger(Expression<? extends Number> expression) {
		check( expression );
		return ( (JpaExpression<?>) expression ).asInteger();
	}

	@Override
	public JpaExpression<Float> toFloat(Expression<? extends Number> expression) {
		check( expression );
		return ( (JpaExpression<?>) expression ).asFloat();
	}

	@Override
	public JpaExpression<Double> toDouble(Expression<? extends Number> expression) {
		check( expression );
		return ( (JpaExpression<?>) expression ).asDouble();
	}

	@Override
	public JpaExpression<BigDecimal> toBigDecimal(Expression<? extends Number> expression) {
		check( expression );
		return ( (JpaExpression<?>) expression ).asBigDecimal();
	}

	@Override
	public JpaExpression<BigInteger> toBigInteger(Expression<? extends Number> expression) {
		check( expression );
		return ( (JpaExpression<?>) expression ).asBigInteger();
	}

	@Override
	public JpaExpression<String> toString(Expression<Character> expression) {
		check( expression );
		return ( (JpaExpression<?>) expression ).asString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> type) {
//		return ( (JoinImplementor) join ).treatAs( type );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type) {
//		return ( (CollectionJoinImplementor) join ).treatAs( type );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type) {
//		return ( (SetJoinImplementor) join ).treatAs( type );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type) {
//		return ( (ListJoinImplementor) join ).treatAs( type );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type) {
//		return ( (MapJoinImplementor) join ).treatAs( type );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X, T extends X> Path<T> treat(Path<X> path, Class<T> type) {
//		return ( (PathImplementor) path ).treatAs( type );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X, T extends X> Root<T> treat(Root<X> root, Class<T> type) {
//		return ( (RootImpl) root ).treatAs( type );
		throw new NotYetImplementedException(  );
	}


	// subqueries ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public Predicate exists(Subquery<?> subquery) {
//		return new ExistsPredicate( this, subquery );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <Y> Expression<Y> all(Subquery<Y> subquery) {
//		return new SubqueryComparisonModifierExpression<Y>(
//				this,
//				(Class<Y>) subquery.getJavaType(),
//				subquery,
//				SubqueryComparisonModifierExpression.Modifier.ALL
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <Y> Expression<Y> some(Subquery<Y> subquery) {
//		return new SubqueryComparisonModifierExpression<Y>(
//				this,
//				(Class<Y>) subquery.getJavaType(),
//				subquery,
//				SubqueryComparisonModifierExpression.Modifier.SOME
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <Y> Expression<Y> any(Subquery<Y> subquery) {
//		return new SubqueryComparisonModifierExpression<Y>(
//				this,
//				(Class<Y>) subquery.getJavaType(),
//				subquery,
//				SubqueryComparisonModifierExpression.Modifier.ANY
//		);
		throw new NotYetImplementedException(  );
	}


	// miscellaneous expressions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	@SuppressWarnings({ "RedundantCast" })
	public <Y> Expression<Y> coalesce(Expression<? extends Y> exp1, Expression<? extends Y> exp2) {
		return coalesce( (Class<Y>) null, exp1, exp2 );
	}

	public <Y> Expression<Y> coalesce(Class<Y> type, Expression<? extends Y> exp1, Expression<? extends Y> exp2) {
//		return new CoalesceExpression<Y>( this, type ).value( exp1 ).value( exp2 );
		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "RedundantCast" })
	public <Y> Expression<Y> coalesce(Expression<? extends Y> exp1, Y exp2) {
		return coalesce( (Class<Y>) null, exp1, exp2 );
	}

	public <Y> Expression<Y> coalesce(Class<Y> type, Expression<? extends Y> exp1, Y exp2) {
//		return new CoalesceExpression<Y>( this, type ).value( exp1 ).value( exp2 );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <T> Coalesce<T> coalesce() {
		return coalesce( (Class<T>)null );
	}

	public <T> Coalesce<T> coalesce(Class<T> type) {
//		return new CoalesceExpression<T>( this, type );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> concat(Expression<String> string1, Expression<String> string2) {
//		return new ConcatExpression( this, string1, string2 );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> concat(Expression<String> string1, String string2) {
//		return new ConcatExpression( this, string1, string2 );
		throw new NotYetImplementedException(  );
	}

	@Override
	public Expression<String> concat(String string1, Expression<String> string2) {
//		return new ConcatExpression( this, string1, string2 );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <Y> Expression<Y> nullif(Expression<Y> exp1, Expression<?> exp2) {
		return nullif( null, exp1, exp2 );
	}

	public <Y> Expression<Y> nullif(Class<Y> type, Expression<Y> exp1, Expression<?> exp2) {
//		return new NullifExpression<Y>( this, type, exp1, exp2 );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <Y> Expression<Y> nullif(Expression<Y> exp1, Y exp2) {
		return nullif( null, exp1, exp2 );
	}

	public <Y> Expression<Y> nullif(Class<Y> type, Expression<Y> exp1, Y exp2) {
//		return new NullifExpression<Y>( this, type, exp1, exp2 );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
		return selectCase( (Class<R>)null, expression );
	}

	public <C, R> SimpleCase<C, R> selectCase(Class<R> type, Expression<? extends C> expression) {
//		return new SimpleCaseExpression<C, R>( this, type, expression );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <R> Case<R> selectCase() {
		return selectCase( (Class<R>)null );
	}

	public <R> Case<R> selectCase(Class<R> type) {
//		return new SearchedCaseExpression<R>( this, type );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <C extends Collection<?>> Expression<Integer> size(C c) {
		int size = c == null ? 0 : c.size();
		return new LiteralExpression<Integer>(this, Integer.class, size);
	}

	@Override
	public <C extends Collection<?>> Expression<Integer> size(Expression<C> exp) {
//		if ( LiteralExpression.class.isInstance(exp) ) {
//			return size( ( (LiteralExpression<C>) exp ).getLiteral() );
//		}
//		else if ( PluralAttributePath.class.isInstance(exp) ) {
//			return new SizeOfCollectionExpression<C>(this, (PluralAttributePath<C>) exp );
//		}
//		// TODO : what other specific types?  any?
//		throw new IllegalArgumentException("unknown collection expression type [" + exp.getClass().getName() + "]" );
		throw new NotYetImplementedException(  );
	}

	@Override
	public <V, M extends Map<?, V>> Expression<Collection<V>> values(M map) {
		return new LiteralExpression<Collection<V>>( this, map.values() );
	}

	@Override
	public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map) {
		return new LiteralExpression<Set<K>>( this, map.keySet() );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <C extends Collection<?>> Predicate isEmpty(Expression<C> collectionExpression) {
//		if ( PluralAttributePath.class.isInstance(collectionExpression) ) {
//			return new IsEmptyPredicate( this, (PluralAttributePath<C>) collectionExpression );
//		}
//		// TODO : what other specific types?  any?
//		throw new IllegalArgumentException(
//				"unknown collection expression type [" + collectionExpression.getClass().getName() + "]"
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	public <C extends Collection<?>> Predicate isNotEmpty(Expression<C> collectionExpression) {
		return isEmpty( collectionExpression ).not();
	}

	@Override
	public <E, C extends Collection<E>> Predicate isMember(E e, Expression<C> collectionExpression) {
//		if ( ! PluralAttributePath.class.isInstance( collectionExpression ) ) {
//			throw new IllegalArgumentException(
//					"unknown collection expression type [" + collectionExpression.getClass().getName() + "]"
//			);
//		}
//		return new MemberOfPredicate<E, C>(
//				this,
//				e,
//				(PluralAttributePath<C>)collectionExpression
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	public <E, C extends Collection<E>> Predicate isNotMember(E e, Expression<C> cExpression) {
		return isMember(e, cExpression).not();
	}

	@Override
	public <E, C extends Collection<E>> Predicate isMember(Expression<E> elementExpression, Expression<C> collectionExpression) {
//		if ( ! PluralAttributePath.class.isInstance( collectionExpression ) ) {
//			throw new IllegalArgumentException(
//					"unknown collection expression type [" + collectionExpression.getClass().getName() + "]"
//			);
//		}
//		return new MemberOfPredicate<E, C>(
//				this,
//				elementExpression,
//				(PluralAttributePath<C>)collectionExpression
//		);
		throw new NotYetImplementedException(  );
	}

	@Override
	public <E, C extends Collection<E>> Predicate isNotMember(Expression<E> eExpression, Expression<C> cExpression) {
		return isMember(eExpression, cExpression).not();
	}


	public void check(Selection<?> selection) {
		if ( !JpaSelection.class.isInstance( selection ) ) {
			throw new CriteriaBuilderException(
					"Expecting javax.persistence.criteria.Selection to be " +
							"org.hibernate.sqm.parser.criteria.tree.select.JpaSelection, but was : " +
							selection.toString()
			);
		}
	}

	private void check(Expression<?> expr) {
		if ( !JpaExpression.class.isInstance( expr ) ) {
			throw new CriteriaBuilderException(
					"Expecting javax.persistence.criteria.Expression to be " +
							"org.hibernate.sqm.parser.criteria.tree.JpaExpression, but was : " +
							expr.toString()
			);
		}
	}

	public void check(Order order) {
		if ( !JpaOrder.class.isInstance( order ) ) {
			throw new CriteriaBuilderException(
					"Expecting javax.persistence.criteria.Order to be " +
							"org.hibernate.sqm.parser.criteria.tree.JpaOrder, but was : " +
							order.toString()
			);
		}
	}
}
