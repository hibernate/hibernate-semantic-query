/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
