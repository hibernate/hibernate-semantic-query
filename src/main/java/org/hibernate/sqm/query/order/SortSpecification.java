/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.order;

import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class SortSpecification {
	private final Expression sortExpression;
	private final String collation;
	private final SortOrder sortOrder;

	public SortSpecification(Expression sortExpression, String collation, SortOrder sortOrder) {
		this.sortExpression = sortExpression;
		this.collation = collation;
		this.sortOrder = sortOrder;
	}

	public SortSpecification(Expression sortExpression) {
		this( sortExpression, null, null );
	}

	public SortSpecification(Expression sortExpression, SortOrder sortOrder) {
		this( sortExpression, null, sortOrder );
	}

	public Expression getSortExpression() {
		return sortExpression;
	}

	public String getCollation() {
		return collation;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}
}
