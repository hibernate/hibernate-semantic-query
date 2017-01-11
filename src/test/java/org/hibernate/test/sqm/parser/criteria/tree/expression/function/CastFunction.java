/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression.function;

import java.io.Serializable;

import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.select.SqmAliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;


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
		implements Serializable {
	public static final String NAME = "cast";

	private final JpaExpression<Y> expressionToCast;

	public CastFunction(
			JpaExpression<Y> expressionToCast,
			SqmExpressableTypeBasic castTargetType,
			Class<T> javaType,
			CriteriaBuilderImpl criteriaBuilder) {
		super( NAME, castTargetType, javaType, criteriaBuilder );
		this.expressionToCast = expressionToCast;
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitCastFunction( expressionToCast, getJavaType() );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, SqmAliasedExpressionContainer container) {
		container.add( visitExpression( visitor ), getAlias() );
	}
}
