/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree;
import java.io.Serializable;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

/**
 * Represents an <tt>ORDER BY</tt> fragment.
 *
 * @author Steve Ebersole
 */
public class OrderImpl implements Order, Serializable {
	private final Expression<?> expression;
	private boolean ascending;

	public OrderImpl(Expression<?> expression) {
		this( expression, true );
	}

	public OrderImpl(Expression<?> expression, boolean ascending) {
		this.expression = expression;
		this.ascending = ascending;
	}

	public Order reverse() {
		ascending = !ascending;
		return this;
	}

	public boolean isAscending() {
		return ascending;
	}

	public Expression<?> getExpression() {
		return expression;
	}
}
