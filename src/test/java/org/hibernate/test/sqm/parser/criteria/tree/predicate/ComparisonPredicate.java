/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;

import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.predicate.ComparisonCriteriaPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.LiteralExpression;

/**
 * Models a basic relational comparison predicate.
 *
 * @author Steve Ebersole
 */
public class ComparisonPredicate
		extends AbstractSimplePredicate
		implements ComparisonCriteriaPredicate, Serializable {
	private final RelationalSqmPredicate.Operator comparisonOperator;
	private final CriteriaExpression<?> leftHandSide;
	private final CriteriaExpression<?> rightHandSide;

	public ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalSqmPredicate.Operator comparisonOperator,
			CriteriaExpression<?> leftHandSide,
			CriteriaExpression<?> rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = rightHandSide;
	}

	@SuppressWarnings({ "unchecked" })
	public ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalSqmPredicate.Operator comparisonOperator,
			CriteriaExpression<?> leftHandSide,
			Object rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = new LiteralExpression( criteriaBuilder, rightHandSide );
	}

	@SuppressWarnings( {"unchecked"})
	public <N extends Number> ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalSqmPredicate.Operator comparisonOperator,
			CriteriaExpression<N> leftHandSide,
			Number rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = new LiteralExpression( criteriaBuilder, rightHandSide );
	}

	public RelationalSqmPredicate.Operator getComparisonOperator() {
		return getComparisonOperator( isNegated() );
	}

	public RelationalSqmPredicate.Operator getComparisonOperator(boolean isNegated) {
		return isNegated
				? comparisonOperator.negate()
				: comparisonOperator;
	}

	@Override
	public CriteriaExpression<?> getLeftHandOperand() {
		return leftHandSide;
	}

	@Override
	public CriteriaExpression<?> getRightHandOperand() {
		return rightHandSide;
	}

	@Override
	public SqmPredicate visitPredicate(CriteriaVisitor visitor) {
		return visitor.visitRelationalPredicate( this );
	}
}
