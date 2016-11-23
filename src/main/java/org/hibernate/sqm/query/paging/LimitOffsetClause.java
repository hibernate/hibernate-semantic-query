/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.paging;

import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.order.SortSpecification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 */
public class LimitOffsetClause {
	private final SqmExpression limitExpression;
	private final SqmExpression offsetExpression;

	public LimitOffsetClause(SqmExpression limitExpression, SqmExpression offsetExpression) {
		this.limitExpression = limitExpression;
		this.offsetExpression = offsetExpression;
	}

	public SqmExpression getLimitExpression() {
		return limitExpression;
	}

	public SqmExpression getOffsetExpression() {
		return offsetExpression;
	}
}
