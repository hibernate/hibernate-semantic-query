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
public class DynamicInstantiationArgument implements AliasedSqmExpression {
	private final SqmExpression selectExpression;
	private final String alias;

	public DynamicInstantiationArgument(SqmExpression selectExpression, String alias) {
		this.selectExpression = selectExpression;
		this.alias = alias;
	}

	public SqmExpression getExpression() {
		return selectExpression;
	}

	public String getAlias() {
		return alias;
	}
}
