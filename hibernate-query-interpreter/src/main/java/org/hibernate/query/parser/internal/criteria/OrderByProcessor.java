/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.criteria;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;

import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortOrder;
import org.hibernate.sqm.query.order.SortSpecification;

/**
 * @author Steve Ebersole
 */
public class OrderByProcessor {
	public static OrderByClause processOrderBy(QuerySpecProcessor querySpecProcessor, CriteriaQuery query) {
		final OrderByClause orderByClause = new OrderByClause();
		if ( !query.getOrderList().isEmpty() ) {
			transferOrderByItems( query, orderByClause, querySpecProcessor );
		}
		return orderByClause;
	}

	private static void transferOrderByItems(
			CriteriaQuery<?> jpaCriteria,
			OrderByClause sqmOrderByClause,
			QuerySpecProcessor querySpecProcessor) {
		for ( Order orderItem : jpaCriteria.getOrderList() ) {
			sqmOrderByClause.addSortSpecification(
					new SortSpecification(
							querySpecProcessor.visitExpression( orderItem.getExpression() ),
							orderItem.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING
					)
			);
		}
	}
}
