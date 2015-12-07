/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import org.hibernate.sqm.query.expression.Expression;

/**
 * Represents an individual selection within a select clause.
 *
 * @author Steve Ebersole
 */
public class Selection implements AliasedExpression {
	private final Expression selectExpression;
	private final String alias;

	public Selection(Expression selectExpression, String alias) {
		this.selectExpression = selectExpression;
		this.alias = alias;
	}

	public Selection(Expression selectExpression) {
		this( selectExpression, null );
	}

	@Override
	public Expression getExpression() {
		return selectExpression;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
