/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.set;

import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class Assignment {
	private final AttributeReferenceSqmExpression stateField;
	private final SqmExpression value;

	public Assignment(AttributeReferenceSqmExpression stateField, SqmExpression value) {

		this.stateField = stateField;
		this.value = value;
	}

	public AttributeReferenceSqmExpression getStateField() {
		return stateField;
	}

	public SqmExpression getValue() {
		return value;
	}
}
