/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.query.SqmInsertStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;
import org.hibernate.sqm.query.from.RootEntityFromElement;

/**
 * Convenience base class for InsertSqmStatement implementations.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractSqmInsertStatement extends AbstractSqmStatement implements SqmInsertStatement {
	private final RootEntityFromElement insertTarget;
	private List<AttributeReferenceSqmExpression> stateFields;

	public AbstractSqmInsertStatement(RootEntityFromElement insertTarget) {
		this.insertTarget = insertTarget;
	}

	@Override
	public RootEntityFromElement getInsertTarget() {
		return insertTarget;
	}

	@Override
	public List<AttributeReferenceSqmExpression> getStateFields() {
		if ( stateFields == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( stateFields );
		}
	}

	public void addInsertTargetStateField(AttributeReferenceSqmExpression stateField) {
		if ( stateFields == null ) {
			stateFields = new ArrayList<>();
		}
		stateFields.add( stateField );
	}
}
