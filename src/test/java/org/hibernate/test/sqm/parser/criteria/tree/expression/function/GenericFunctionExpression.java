/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression.function;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.function.GenericFunctionCriteriaExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.select.AliasedSqmExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * Models the basic concept of a SQL function.
 *
 * @author Steve Ebersole
 */
public class GenericFunctionExpression<X>
		extends AbstractFunctionExpression<X>
		implements GenericFunctionCriteriaExpression<X> {
	private final List<CriteriaExpression<?>> arguments;

	public GenericFunctionExpression(
			String functionName,
			BasicType sqmType,
			Class<X> javaType,
			CriteriaBuilderImpl criteriaBuilder) {
		this( functionName, sqmType, javaType, criteriaBuilder, Collections.<CriteriaExpression<?>>emptyList() );
	}

	public GenericFunctionExpression(
			String functionName,
			BasicType sqmType,
			Class<X> javaType,
			CriteriaBuilderImpl criteriaBuilder,
			CriteriaExpression<?>... arguments) {
		this( functionName, sqmType, javaType, criteriaBuilder, Arrays.asList( arguments ) );
	}

	public GenericFunctionExpression(
			String functionName,
			BasicType sqmType,
			Class<X> javaType,
			CriteriaBuilderImpl criteriaBuilder,
			List<CriteriaExpression<?>> arguments) {
		super( functionName, sqmType, javaType, criteriaBuilder);
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
	public List<CriteriaExpression<?>> getArguments() {
		return arguments;
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitGenericFunction( this );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedSqmExpressionContainer container) {
		container.add( visitExpression( visitor ), getAlias() );
	}
}
