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

import org.hibernate.sqm.parser.common.AttributeBinding;
import org.hibernate.sqm.query.SqmInsertStatement;
import org.hibernate.sqm.query.from.SqmRoot;

/**
 * Convenience base class for InsertSqmStatement implementations.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractSqmInsertStatement extends AbstractSqmStatement implements SqmInsertStatement {
	private final SqmRoot insertTarget;
	private List<AttributeBinding> stateFields;

	public AbstractSqmInsertStatement(SqmRoot insertTarget) {
		this.insertTarget = insertTarget;
	}

	@Override
	public SqmRoot getInsertTarget() {
		return insertTarget;
	}

	@Override
	public List<AttributeBinding> getStateFields() {
		if ( stateFields == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( stateFields );
		}
	}

	public void addInsertTargetStateField(AttributeBinding stateField) {
		if ( stateFields == null ) {
			stateFields = new ArrayList<>();
		}
		stateFields.add( stateField );
	}
}
