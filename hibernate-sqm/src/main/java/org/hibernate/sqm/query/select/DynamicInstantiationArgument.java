/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.select;

import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class DynamicInstantiationArgument implements AliasedExpression {
	private final Expression selectExpression;
	private final String alias;

	public DynamicInstantiationArgument(Expression selectExpression, String alias) {
		this.selectExpression = selectExpression;
		this.alias = alias;
	}

	public Expression getExpression() {
		return selectExpression;
	}

	public String getAlias() {
		return alias;
	}
}
