/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression.function;

import java.io.Serializable;

import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.query.expression.Expression;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.ExpressionImpl;


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
		implements FunctionExpression<T>, Serializable {
	public static final String CAST_NAME = "cast";

	private final ExpressionImpl<Y> castSource;

	public CastFunction(
			CriteriaBuilderImpl criteriaBuilder,
			Class<T> javaType,
			ExpressionImpl<Y> castSource) {
		super( criteriaBuilder, CAST_NAME, javaType );
		this.castSource = castSource;
	}

	public ExpressionImpl<Y> getCastSource() {
		return castSource;
	}

	@Override
	public Expression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitFunction(
				CAST_NAME,
				criteriaBuilder().consumerContext().getDomainMetamodel().getBasicType( getJavaType() ),
				getCastSource()
		);
	}
}
