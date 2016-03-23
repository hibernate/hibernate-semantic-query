/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.expression;

import java.io.Serializable;
import javax.persistence.criteria.Expression;

/**
 * Contract for operators with a single operand.
 *
 * @author Steve Ebersole
 */
public interface UnaryOperatorExpression<T> extends ExpressionImplementor<T>, Serializable {
	/**
	 * Get the operand.
	 *
	 * @return The operand.
	 */
	Expression<?> getOperand();
}
