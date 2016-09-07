/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.query.from.SqmFromClause;

/**
 * @author Steve Ebersole
 */
public interface QuerySpecProcessingState extends FromElementLocator, ResolutionContext {
	QuerySpecProcessingState getParent();
	SqmFromClause getFromClause();
}
