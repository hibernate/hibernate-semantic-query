/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi.expression;

/**
 * Contract for operators with two operands.
 *
 * @author Steve Ebersole
 */
public interface BinaryOperatorCriteriaExpression<T> extends CriteriaExpression<T> {
	/**
	 * Get the right-hand operand.
	 *
	 * @return The right-hand operand.
	 */
	CriteriaExpression<?> getRightHandOperand();

	/**
	 * Get the left-hand operand.
	 *
	 * @return The left-hand operand.
	 */
	CriteriaExpression<?> getLeftHandOperand();
}
