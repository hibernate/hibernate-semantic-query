/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import javax.persistence.criteria.Expression;

import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.BinaryOperatorExpression;
import org.hibernate.test.sqm.parser.criteria.tree.expression.LiteralExpression;

/**
 * Models a basic relational comparison predicate.
 *
 * @author Steve Ebersole
 */
public class ComparisonPredicate
		extends AbstractSimplePredicate
		implements BinaryOperatorExpression<Boolean>, Serializable {
	private final RelationalPredicate.Operator comparisonOperator;
	private final Expression<?> leftHandSide;
	private final Expression<?> rightHandSide;

	public ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalPredicate.Operator comparisonOperator,
			Expression<?> leftHandSide,
			Expression<?> rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = rightHandSide;
	}

	@SuppressWarnings({ "unchecked" })
	public ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalPredicate.Operator comparisonOperator,
			Expression<?> leftHandSide,
			Object rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = new LiteralExpression( criteriaBuilder, rightHandSide );
	}

	@SuppressWarnings( {"unchecked"})
	public <N extends Number> ComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			RelationalPredicate.Operator comparisonOperator,
			Expression<N> leftHandSide,
			Number rightHandSide) {
		super( criteriaBuilder );
		this.comparisonOperator = comparisonOperator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = new LiteralExpression( criteriaBuilder, rightHandSide );
	}

	public RelationalPredicate.Operator getComparisonOperator() {
		return getComparisonOperator( isNegated() );
	}

	public RelationalPredicate.Operator getComparisonOperator(boolean isNegated) {
		return isNegated
				? comparisonOperator.negate()
				: comparisonOperator;
	}

	@Override
	public Expression getLeftHandOperand() {
		return leftHandSide;
	}

	@Override
	public Expression getRightHandOperand() {
		return rightHandSide;
	}

	@Override
	public Predicate visitPredicate(CriteriaVisitor visitor) {
		return visitor.visitRelationalPredicate(
				getLeftHandOperand(),
				getComparisonOperator(),
				getRightHandOperand()
		);
	}
}
