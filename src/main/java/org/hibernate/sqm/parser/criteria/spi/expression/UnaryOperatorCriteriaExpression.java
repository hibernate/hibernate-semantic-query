/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi.expression;

import java.io.Serializable;

/**
 * Contract for operators with a single operand.
 *
 * @author Steve Ebersole
 */
public interface UnaryOperatorCriteriaExpression<T> extends CriteriaExpression<T>, Serializable {
	/**
	 * Get the operand.
	 *
	 * @return The operand.
	 */
	CriteriaExpression<?> getOperand();
}
