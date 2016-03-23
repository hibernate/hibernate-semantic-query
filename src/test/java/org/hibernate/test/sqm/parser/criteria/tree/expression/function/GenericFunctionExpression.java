/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression.function;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.select.AliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.ExpressionImpl;

/**
 * Models the basic concept of a SQL function.
 *
 * @author Steve Ebersole
 */
public class GenericFunctionExpression<X> extends AbstractFunctionExpression<X> {
	private final List<javax.persistence.criteria.Expression<?>> arguments;

	public GenericFunctionExpression(
			CriteriaBuilderImpl criteriaBuilder,
			String functionName,
			Class<X> javaType) {
		this( criteriaBuilder, functionName, javaType, Collections.<javax.persistence.criteria.Expression<?>>emptyList() );
	}

	public GenericFunctionExpression(
			CriteriaBuilderImpl criteriaBuilder,
			String functionName,
			Class<X> javaType,
			javax.persistence.criteria.Expression<?>... arguments) {
		this( criteriaBuilder, functionName, javaType, Arrays.asList( arguments ) );
	}

	public GenericFunctionExpression(
			CriteriaBuilderImpl criteriaBuilder,
			String functionName,
			Class<X> javaType,
			List<javax.persistence.criteria.Expression<?>> arguments) {
		super( criteriaBuilder, functionName, javaType );
		this.arguments = arguments;
	}

	protected  static int properSize(int number) {
		return number + (int)( number*.75 ) + 1;
	}

	@Override
	public boolean isAggregation() {
		return false;
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedExpressionContainer container) {
		container.add( visitExpression( visitor ), getAlias() );
	}

	@Override
	public Expression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitFunction(  
				getFunctionName(),
				criteriaBuilder().consumerContext().getDomainMetamodel().getBasicType( getJavaType() ),
				arguments
		);
	}
}
