/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.set;

import org.hibernate.sqm.query.expression.domain.SingularAttributeReference;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmAssignment {
	private final SingularAttributeReference stateField;
	private final SqmExpression value;

	public SqmAssignment(SingularAttributeReference stateField, SqmExpression value) {
		this.stateField = stateField;
		this.value = value;
	}

	public SingularAttributeReference getStateField() {
		return stateField;
	}

	public SqmExpression getValue() {
		return value;
	}
}
