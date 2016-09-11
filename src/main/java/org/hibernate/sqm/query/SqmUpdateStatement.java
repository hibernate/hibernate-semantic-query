/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.predicate.SqmWhereClauseContainer;
import org.hibernate.sqm.query.set.SqmSetClause;

/**
 * @author Steve Ebersole
 */
public interface SqmUpdateStatement extends SqmStatementNonSelect, SqmWhereClauseContainer {
	RootEntityFromElement getEntityFromElement();
	SqmSetClause getSetClause();
	SqmWhereClause getWhereClause();
}
