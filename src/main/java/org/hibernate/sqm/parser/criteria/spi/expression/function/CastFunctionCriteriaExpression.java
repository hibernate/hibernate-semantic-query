/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi.expression.function;

import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;

/**
 * Represents a CAST function in the JPA criteria tree.  This object's
 * {@link #getExpressionSqmType()} represents the cast target type.
 *
 * @author Steve Ebersole
 */
public interface CastFunctionCriteriaExpression<T,Y> extends FunctionCriteriaExpression<T> {
	CriteriaExpression<Y> getExpressionToCast();
}
