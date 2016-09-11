/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.SqmWhereClauseContainer;

/**
 * @author Steve Ebersole
 */
public interface SqmDeleteStatement extends SqmStatementNonSelect, SqmWhereClauseContainer {
	RootEntityFromElement getEntityFromElement();
}
