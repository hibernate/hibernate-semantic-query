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
public class SqmSortSpecification {
	private final SqmExpression sortExpression;
	private final String collation;
	private final SqmSortOrder sortOrder;

	public SqmSortSpecification(SqmExpression sortExpression, String collation, SqmSortOrder sortOrder) {
		this.sortExpression = sortExpression;
		this.collation = collation;
		this.sortOrder = sortOrder;
	}

	public SqmSortSpecification(SqmExpression sortExpression) {
		this( sortExpression, null, null );
	}

	public SqmSortSpecification(SqmExpression sortExpression, SqmSortOrder sortOrder) {
		this( sortExpression, null, sortOrder );
	}

	public SqmExpression getSortExpression() {
		return sortExpression;
	}

	public String getCollation() {
		return collation;
	}

	public SqmSortOrder getSortOrder() {
		return sortOrder;
	}
}
