/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;

import org.hibernate.query.sqm.produce.spi.criteria.CriteriaVisitor;
import org.hibernate.query.sqm.produce.spi.criteria.JpaExpression;
import org.hibernate.query.sqm.produce.spi.criteria.JpaPredicate;
import org.hibernate.query.sqm.tree.predicate.RelationalPredicateOperator;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.LiteralExpression;

/**
 * Models a basic relational comparison predicate.
 *
 * @author Steve Ebersole
 */
public class ComparisonPredicate
		extends AbstractSimplePredicate
		implements JpaPredicate, Serializable {
	private final RelationalPredicateOperator comparisonOperator;
	private final JpaExpression<?> leftHandSide;
	private final JpaExpression<?> rightHandSide;

	public ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalPredicateOperator comparisonOperator,
			JpaExpression<?> leftHandSide,
			JpaExpression<?> rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = rightHandSide;
	}

	@SuppressWarnings({ "unchecked" })
	public ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalPredicateOperator comparisonOperator,
			JpaExpression<?> leftHandSide,
			Object rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = new LiteralExpression( criteriaBuilder, rightHandSide );
	}

	@SuppressWarnings( {"unchecked"})
	public <N extends Number> ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalPredicateOperator comparisonOperator,
			JpaExpression<N> leftHandSide,
			Number rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = new LiteralExpression( criteriaBuilder, rightHandSide );
	}

	public RelationalPredicateOperator getComparisonOperator() {
		return getComparisonOperator( isNegated() );
	}

	public RelationalPredicateOperator getComparisonOperator(boolean isNegated) {
		return isNegated
				? comparisonOperator.negate()
				: comparisonOperator;
	}

	public JpaExpression<?> getLeftHandOperand() {
		return leftHandSide;
	}

	public JpaExpression<?> getRightHandOperand() {
		return rightHandSide;
	}

	@Override
	public SqmPredicate visitPredicate(CriteriaVisitor visitor) {
		return visitor.visitRelationalPredicate( comparisonOperator, leftHandSide, rightHandSide );
	}
}
