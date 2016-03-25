/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * Represents an individual selection within a select clause.
 *
 * @author Steve Ebersole
 */
public class Selection implements AliasedSqmExpression {
	private final SqmExpression selectExpression;
	private final String alias;

	public Selection(SqmExpression selectExpression, String alias) {
		this.selectExpression = selectExpression;
		this.alias = alias;
	}

	public Selection(SqmExpression selectExpression) {
		this( selectExpression, null );
	}

	@Override
	public SqmExpression getExpression() {
		return selectExpression;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
