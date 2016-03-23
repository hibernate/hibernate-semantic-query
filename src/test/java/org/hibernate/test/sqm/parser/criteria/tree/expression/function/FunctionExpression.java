/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression.function;

import org.hibernate.test.sqm.parser.criteria.tree.expression.ExpressionImplementor;

/**
 * Contract for expressions which model a SQL function call.
 *
 * @param <T> The type of the function result.
 *
 * @author Steve Ebersole
 */
public interface FunctionExpression<T> extends ExpressionImplementor<T> {
	/**
	 * Retrieve the name of the function.
	 *
	 * @return The function name.
	 */
	String getFunctionName();

	/**
	 * Is this function a value aggregator (like a <tt>COUNT</tt> or <tt>MAX</tt> function e.g.)?
	 *
	 * @return True if this functions does aggregation.
	 */
	boolean isAggregation();
}
