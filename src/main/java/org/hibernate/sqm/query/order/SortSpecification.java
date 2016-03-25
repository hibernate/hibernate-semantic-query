/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.order;

import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SortSpecification {
	private final SqmExpression sortExpression;
	private final String collation;
	private final SortOrder sortOrder;

	public SortSpecification(SqmExpression sortExpression, String collation, SortOrder sortOrder) {
		this.sortExpression = sortExpression;
		this.collation = collation;
		this.sortOrder = sortOrder;
	}

	public SortSpecification(SqmExpression sortExpression) {
		this( sortExpression, null, null );
	}

	public SortSpecification(SqmExpression sortExpression, SortOrder sortOrder) {
		this( sortExpression, null, sortOrder );
	}

	public SqmExpression getSortExpression() {
		return sortExpression;
	}

	public String getCollation() {
		return collation;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}
}
