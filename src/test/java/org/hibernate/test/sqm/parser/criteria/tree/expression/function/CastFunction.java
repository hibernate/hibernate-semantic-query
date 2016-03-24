/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression.function;

import java.io.Serializable;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.function.CastFunctionCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.function.FunctionCriteriaExpression;
import org.hibernate.sqm.query.expression.Expression;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractCriteriaExpressionImpl;


/**
 * Models a <tt>CAST</tt> function.
 *
 * @param <T> The cast result type.
 * @param <Y> The type of the expression to be cast.
 *
 * @author Steve Ebersole
 */
public class CastFunction<T,Y>
		extends AbstractFunctionExpression<T>
		implements CastFunctionCriteriaExpression<T,Y>, Serializable {
	public static final String NAME = "cast";

	private final AbstractCriteriaExpressionImpl<Y> expressionToCast;

	public CastFunction(
			AbstractCriteriaExpressionImpl<Y> expressionToCast,
			BasicType<T> castTargetType,
			Class<T> javaType,
			CriteriaBuilderImpl criteriaBuilder) {
		super( NAME, castTargetType, javaType, criteriaBuilder );
		this.expressionToCast = expressionToCast;
	}

	@Override
	public CriteriaExpression<Y> getExpressionToCast() {
		return expressionToCast;
	}

	@Override
	public Expression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitCastFunction( this );
	}
}
