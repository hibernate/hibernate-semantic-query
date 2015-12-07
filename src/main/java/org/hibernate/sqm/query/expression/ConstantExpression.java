/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.domain.BasicType;

/**
 * @author Steve Ebersole
 */
public interface ConstantExpression<T> extends ImpliedTypeExpression {
	T getValue();

	@Override
	BasicType<T> getExpressionType();
}
