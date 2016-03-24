/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi.predicate;

import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;

/**
 * @author Steve Ebersole
 */
public interface UnaryCriteriaPredicate {
	/**
	 * Get the operand.
	 *
	 * @return The operand.
	 */
	CriteriaExpression<?> getOperand();
}
