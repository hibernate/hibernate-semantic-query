/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class SetClause {
	private List<Assignment> assignments = new ArrayList<Assignment>();

	public List<Assignment> getAssignments() {
		return Collections.unmodifiableList( assignments );
	}

	public void addAssignment(Assignment assignment) {
		assignments.add( assignment );
	}

	public void addAssignment(AttributeReferenceExpression stateField, Expression value) {
		addAssignment( new Assignment( stateField, value ) );
	}
}
