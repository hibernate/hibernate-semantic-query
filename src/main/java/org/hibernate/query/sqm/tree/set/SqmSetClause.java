/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmSetClause {
	private List<SqmAssignment> assignments = new ArrayList<>();

	public List<SqmAssignment> getAssignments() {
		return Collections.unmodifiableList( assignments );
	}

	public void addAssignment(SqmAssignment assignment) {
		assignments.add( assignment );
	}

	public void addAssignment(SqmSingularAttributeReference stateField, SqmExpression value) {
		addAssignment( new SqmAssignment( stateField, value ) );
	}
}
