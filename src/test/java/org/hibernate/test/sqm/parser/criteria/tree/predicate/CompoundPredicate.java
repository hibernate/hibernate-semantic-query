/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.tree.JpaPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;

import org.hibernate.test.sqm.domain.BasicType;
import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * A compound {@link Predicate predicate} is a grouping of other {@link Predicate predicates} in order to convert
 * either a conjunction (logical AND) or a disjunction (logical OR).
 *
 * @author Steve Ebersole
 */
public class CompoundPredicate
		extends AbstractPredicateImpl
		implements Serializable {
	private BooleanOperator operator;
	private final List<JpaPredicate> predicates = new ArrayList<>();

	/**
	 * Constructs an empty conjunction or disjunction.
	 *
	 * @param criteriaBuilder The query builder from which this originates.
	 * @param operator Indicates whether this predicate will function
	 * as a conjunction or disjunction.
	 */
	public CompoundPredicate(CriteriaBuilderImpl criteriaBuilder, BooleanOperator operator) {
		super( criteriaBuilder,
			   (BasicType<Boolean>) criteriaBuilder.consumerContext().getDomainMetamodel().resolveBasicType( Boolean.class )
		);
		this.operator = operator;
	}

	/**
	 * Constructs a conjunction or disjunction over the given expressions.
	 *
	 * @param criteriaBuilder The query builder from which this originates.
	 * @param operator Indicates whether this predicate will function
	 * as a conjunction or disjunction.
	 * @param expressions The expressions to be grouped.
	 */
	public CompoundPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			BooleanOperator operator,
			Expression<Boolean>... expressions) {
		this( criteriaBuilder, operator );
		applyExpressions( expressions );
	}

	/**
	 * Constructs a conjunction or disjunction over the given expressions.
	 *
	 * @param criteriaBuilder The query builder from which this originates.
	 * @param operator Indicates whether this predicate will function
	 * as a conjunction or disjunction.
	 * @param expressions The expressions to be grouped.
	 */
	public CompoundPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			BooleanOperator operator,
			List<Expression<Boolean>> expressions) {
		this( criteriaBuilder, operator );
		applyExpressions( expressions );
	}

	private void applyExpressions(Expression<Boolean>... expressions) {
		applyExpressions( Arrays.asList( expressions ) );
	}

	private void applyExpressions(List<Expression<Boolean>> expressions) {
		this.predicates.clear();
		expressions.forEach( expr -> this.predicates.add( criteriaBuilder().wrap( expr ) ) );
	}

	@Override
	public BooleanOperator getOperator() {
		return operator;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return predicates.stream().collect( Collectors.toList() );
	}

	public boolean isJunction() {
		return true;
	}

	/**
	 * Create negation of compound predicate by using logic rules:
	 * 1. not (x || y) is (not x && not y)
	 * 2. not (x && y) is (not x || not y)
	 */
	@Override
	public JpaPredicate not() {
		return new NegatedPredicateWrapper( this );
	}

	private void toggleOperator() {
		this.operator = reverseOperator( this.operator );
	}

	public static BooleanOperator reverseOperator(BooleanOperator operator) {
		return operator == BooleanOperator.AND
				? BooleanOperator.OR
				: BooleanOperator.AND;
	}

	private static String operatorTextWithSeparator(BooleanOperator operator) {
		return operator == BooleanOperator.AND
				? " and "
				: " or ";
	}

	@Override
	public SqmPredicate visitPredicate(CriteriaVisitor visitor) {
		if ( operator == BooleanOperator.AND ) {
			return visitor.visitAndPredicate( predicates );
		}
		else {
			return visitor.visitOrPredicate( predicates );
		}
	}
}
