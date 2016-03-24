/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import javax.persistence.criteria.Expression;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.BooleanExpressionCriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;
import org.hibernate.sqm.query.predicate.Predicate;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;


/**
 * Defines a {@link javax.persistence.criteria.Predicate} used to wrap an {@link Expression Expression&lt;Boolean&gt;}.
 * 
 * @author Steve Ebersole
 */
public class BooleanExpressionPredicate
		extends AbstractSimplePredicate
		implements BooleanExpressionCriteriaPredicate, Serializable {
	private final CriteriaExpression<Boolean> expression;
	private final Boolean assertedValue;

	public BooleanExpressionPredicate(CriteriaBuilderImpl criteriaBuilder, CriteriaExpression<Boolean> expression) {
		this( criteriaBuilder, expression, Boolean.TRUE );
	}

	@SuppressWarnings("unchecked")
	public BooleanExpressionPredicate(
			CriteriaBuilderImpl criteriaBuilder,
			CriteriaExpression<Boolean> expression,
			Boolean assertedValue) {
		super( criteriaBuilder, (BasicType<Boolean>) expression.getExpressionSqmType() );
		this.expression = expression;
		this.assertedValue = assertedValue;
	}

	@Override
	public CriteriaExpression<Boolean> getOperand() {
		return expression;
	}

	public Boolean getAssertedValue() {
		return assertedValue;
	}

	@Override
	public Predicate visitPredicate(CriteriaVisitor visitor) {
		return visitor.visitBooleanExpressionPredicate( this );
	}
}
