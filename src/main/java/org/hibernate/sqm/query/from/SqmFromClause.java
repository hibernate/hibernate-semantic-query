/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import java.util.ArrayList;
import java.util.List;

/**
 * Contract representing a from clause.
 * <p/>
 * The parent/child bit represents sub-queries.  The child from clauses are only used for test assertions,
 * but are left here as it is most convenient to maintain them here versus another structure.
 *
 * @author Steve Ebersole
 */
public class SqmFromClause {
	private List<FromElementSpace> fromElementSpaces = new ArrayList<FromElementSpace>();

	public List<FromElementSpace> getFromElementSpaces() {
		return fromElementSpaces;
	}

	public void addFromElementSpace(FromElementSpace space) {

	}
	public FromElementSpace makeFromElementSpace() {
		final FromElementSpace space = new FromElementSpace( this );
		fromElementSpaces.add( space );
		return space;
	}
}
