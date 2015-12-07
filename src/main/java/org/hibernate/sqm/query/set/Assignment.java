/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.set;

import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class Assignment {
	private final AttributeReferenceExpression stateField;
	private final Expression value;

	public Assignment(AttributeReferenceExpression stateField, Expression value) {

		this.stateField = stateField;
		this.value = value;
	}

	public AttributeReferenceExpression getStateField() {
		return stateField;
	}

	public Expression getValue() {
		return value;
	}
}
