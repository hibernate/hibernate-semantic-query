/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import org.hibernate.sqm.query.expression.Expression;

/**
 * Represents any aliased expression
 *
 * @author Steve Ebersole
 */
public interface AliasedExpression {
	Expression getExpression();
	String getAlias();
}
