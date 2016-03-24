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
import org.hibernate.sqm.parser.criteria.spi.expression.function.FunctionCriteriaExpression;
import org.hibernate.sqm.query.select.AliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractCriteriaExpressionImpl;

/**
 * Models the basic concept of a SQL function.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractFunctionExpression<X>
		extends AbstractCriteriaExpressionImpl<X>
		implements FunctionCriteriaExpression<X>, Serializable {

	private final String functionName;

	public AbstractFunctionExpression(
			String functionName,
			BasicType sqmType,
			Class<X> javaType,
			CriteriaBuilderImpl criteriaBuilder) {
		super( criteriaBuilder, sqmType, javaType );
		this.functionName = functionName;
	}

	protected  static int properSize(int number) {
		return number + (int)( number*.75 ) + 1;
	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public BasicType<X> getExpressionSqmType() {
		return (BasicType<X>) super.getExpressionSqmType();
	}

	@Override
	public BasicType<X> getFunctionResultType() {
		return getExpressionSqmType();
	}

	@Override
	public boolean isAggregation() {
		return false;
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedExpressionContainer container) {
		container.add( visitExpression( visitor ), getAlias() );
	}
}
