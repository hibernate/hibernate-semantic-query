/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromClauseContainer;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.predicate.WhereClauseContainer;
import org.hibernate.sqm.query.select.SelectClause;

/**
 * Defines the commonality between a root query and a subquery.
 *
 * @author Steve Ebersole
 */
public class QuerySpec implements FromClauseContainer, WhereClauseContainer {
	private final FromClause fromClause;
	private final SelectClause selectClause;
	private final WhereClause whereClause;

	// group-by
	// having


	public QuerySpec(
			FromClause fromClause,
			SelectClause selectClause,
			WhereClause whereClause) {
		this.fromClause = fromClause;
		this.selectClause = selectClause;
		this.whereClause = whereClause;
	}

	public SelectClause getSelectClause() {
		return selectClause;
	}

	@Override
	public FromClause getFromClause() {
		return fromClause;
	}

	@Override
	public WhereClause getWhereClause() {
		return whereClause;
	}
}
