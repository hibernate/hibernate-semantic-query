/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmFromClauseContainer;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.paging.LimitOffsetClause;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.predicate.SqmWhereClauseContainer;
import org.hibernate.sqm.query.select.SqmSelectClause;

/**
 * Defines the commonality between a root query and a subquery.
 *
 * @author Steve Ebersole
 */
public class SqmQuerySpec implements SqmFromClauseContainer, SqmWhereClauseContainer {
	private final SqmFromClause fromClause;
	private final SqmSelectClause selectClause;
	private final SqmWhereClause whereClause;
	private final OrderByClause orderByClause;
	private final LimitOffsetClause limitOffsetClause;

	// todo : group-by + having

	public SqmQuerySpec(
			SqmFromClause fromClause,
			SqmSelectClause selectClause,
			SqmWhereClause whereClause,
			OrderByClause orderByClause,
			LimitOffsetClause limitOffsetClause) {
		this.fromClause = fromClause;
		this.selectClause = selectClause;
		this.whereClause = whereClause;
		this.orderByClause = orderByClause;
		this.limitOffsetClause = limitOffsetClause;
	}

	public SqmSelectClause getSelectClause() {
		return selectClause;
	}

	@Override
	public SqmFromClause getFromClause() {
		return fromClause;
	}

	@Override
	public SqmWhereClause getWhereClause() {
		return whereClause;
	}

	public OrderByClause getOrderByClause() {
		return orderByClause;
	}

	public LimitOffsetClause getLimitOffsetClause() {
		return limitOffsetClause;
	}
}
