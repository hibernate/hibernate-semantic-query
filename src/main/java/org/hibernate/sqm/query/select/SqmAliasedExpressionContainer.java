/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public interface SqmAliasedExpressionContainer<T extends SqmAliasedExpression> {
	T add(SqmExpression expression, String alias);
	void add(T aliasExpression);
}
