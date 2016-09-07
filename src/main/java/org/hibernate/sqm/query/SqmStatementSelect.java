/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.order.OrderByClause;

/**
 * @author Steve Ebersole
 */
public class SqmStatementSelect implements SqmStatement {
	private SqmQuerySpec querySpec;
	private OrderByClause orderByClause;

	public SqmStatementSelect() {
	}

	public SqmQuerySpec getQuerySpec() {
		return querySpec;
	}

	public OrderByClause getOrderByClause() {
		return orderByClause;
	}

	public void applyQuerySpec(SqmQuerySpec querySpec) {
		if ( this.querySpec != null ) {
			throw new IllegalStateException( "SqmQuerySpec was already defined for select-statement" );
		}
		this.querySpec = querySpec;
	}

	public void applyOrderByClause(OrderByClause orderByClause) {
		if ( this.orderByClause != null ) {
			throw new IllegalStateException( "OrderByClause was already defined for select-statement" );
		}
		this.orderByClause = orderByClause;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSelectStatement( this );
	}
}
