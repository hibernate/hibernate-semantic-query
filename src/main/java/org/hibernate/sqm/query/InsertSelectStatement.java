/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;
import org.hibernate.sqm.query.from.RootEntityFromElement;

/**
 * @author Steve Ebersole
 */
public class InsertSelectStatement implements InsertStatement {
	private final RootEntityFromElement insertTarget;
	private List<AttributeReferenceSqmExpression> stateFields;
	private QuerySpec selectQuery;

	public InsertSelectStatement(RootEntityFromElement insertTarget) {
		this.insertTarget = insertTarget;
	}

	@Override
	public Type getType() {
		return Type.INSERT;
	}

	@Override
	public RootEntityFromElement getInsertTarget() {
		return insertTarget;
	}

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
			stateFields = new ArrayList<AttributeReferenceSqmExpression>();
		}
		stateFields.add( stateField );
	}

	public QuerySpec getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(QuerySpec selectQuery) {
		this.selectQuery = selectQuery;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInsertSelectStatement( this );
	}
}
