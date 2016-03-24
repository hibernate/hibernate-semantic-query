/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi.expression;

import javax.persistence.criteria.Expression;

import org.hibernate.sqm.parser.criteria.spi.predicate.UnaryCriteriaPredicate;

/**
 * Defines a {@link javax.persistence.criteria.Predicate} used to wrap an {@link Expression Expression&lt;Boolean&gt;}.
 *
 * @author Steve Ebersole
 */
public interface BooleanExpressionCriteriaPredicate extends UnaryCriteriaPredicate {
	@Override
	CriteriaExpression<Boolean> getOperand();
}
