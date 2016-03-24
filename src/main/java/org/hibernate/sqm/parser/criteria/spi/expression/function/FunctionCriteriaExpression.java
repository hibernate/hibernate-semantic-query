/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi.expression.function;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;

/**
 * Contract for expressions which model a SQL function call.
 *
 * @param <T> The type of the function result.
 *
 * @author Steve Ebersole
 */
public interface FunctionCriteriaExpression<T> extends CriteriaExpression<T> {
	/**
	 * Retrieve the name of the function.
	 *
	 * @return The function name.
	 */
	String getFunctionName();

	@Override
	BasicType<T> getExpressionSqmType();

	BasicType<T> getFunctionResultType();

	/**
	 * Is this function a value aggregator (like a <tt>COUNT</tt> or <tt>MAX</tt> function e.g.)?
	 *
	 * @return True if this functions does aggregation.
	 */
	boolean isAggregation();
}
