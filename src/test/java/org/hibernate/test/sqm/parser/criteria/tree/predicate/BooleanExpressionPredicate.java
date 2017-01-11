/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import javax.persistence.criteria.Expression;

import org.hibernate.orm.type.spi.BasicType;
import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.parser.criteria.tree.JpaPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;


/**
 * Defines a {@link javax.persistence.criteria.Predicate} used to wrap an {@link Expression Expression&lt;Boolean&gt;}.
 * 
 * @author Steve Ebersole
 */
public class BooleanExpressionPredicate
		extends AbstractSimplePredicate
		implements JpaPredicate, Serializable {
	private final JpaExpression<Boolean> expression;
	private final Boolean assertedValue;

	public BooleanExpressionPredicate(CriteriaBuilderImpl criteriaBuilder, JpaExpression<Boolean> expression) {
		this( criteriaBuilder, expression, Boolean.TRUE );
	}

	@SuppressWarnings("unchecked")
	public BooleanExpressionPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			JpaExpression<Boolean> expression,
			Boolean assertedValue) {
		super( criteriaBuilder, (BasicType<Boolean>) expression.getExpressionSqmType() );
		this.expression = expression;
		this.assertedValue = assertedValue;
	}

	public JpaExpression<Boolean> getOperand() {
		return expression;
	}

	public Boolean getAssertedValue() {
		return assertedValue;
	}

	@Override
	public SqmPredicate visitPredicate(CriteriaVisitor visitor) {
		return visitor.visitBooleanExpressionPredicate( expression, assertedValue );
	}
}
