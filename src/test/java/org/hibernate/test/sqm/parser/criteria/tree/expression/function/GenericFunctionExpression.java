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

import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.query.expression.SqmExpression;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;

/**
 * Models the basic concept of a SQL function.
 *
 * @author Steve Ebersole
 */
public class GenericFunctionExpression<X>
		extends AbstractFunctionExpression<X>
		implements JpaExpression<X> {
	private final List<JpaExpression<?>> arguments;

	public GenericFunctionExpression(
			String functionName,
			SqmExpressableTypeBasic sqmType,
			Class<X> javaType,
			CriteriaBuilderImpl criteriaBuilder) {
		this( functionName, sqmType, javaType, criteriaBuilder, Collections.emptyList() );
	}

	public GenericFunctionExpression(
			String functionName,
			SqmExpressableTypeBasic sqmType,
			Class<X> javaType,
			CriteriaBuilderImpl criteriaBuilder,
			JpaExpression<?>... arguments) {
		this( functionName, sqmType, javaType, criteriaBuilder, Arrays.asList( arguments ) );
	}

	public GenericFunctionExpression(
			String functionName,
			SqmExpressableTypeBasic sqmType,
			Class<X> javaType,
			CriteriaBuilderImpl criteriaBuilder,
			List<JpaExpression<?>> arguments) {
		super( functionName, sqmType, javaType, criteriaBuilder);
		this.arguments = arguments;
	}

	protected  static int properSize(int number) {
		return number + (int)( number*.75 ) + 1;
	}

	public List<JpaExpression<?>> getArguments() {
		return arguments;
	}

	@Override
	public SqmExpressableTypeBasic getFunctionResultType() {
		return (SqmDomainTypeBasic) super.getFunctionResultType();
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		return visitor.visitGenericFunction(
				getFunctionName(),
				getFunctionResultType(),
				getArguments()
		);
	}
}
