/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import java.util.List;

import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;
import org.hibernate.sqm.query.from.SqmRoot;

/**
 * The general contract for INSERT statements.  At the moment only the INSERT-SELECT
 * forms is implemented/supported.
 *
 * @author Steve Ebersole
 */
public interface SqmInsertStatement extends SqmStatement {
	SqmRoot getInsertTarget();
	List<AttributeReferenceSqmExpression> getStateFields();
}
