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
	private List<SqmFromElementSpace> fromElementSpaces = new ArrayList<SqmFromElementSpace>();

	public List<SqmFromElementSpace> getFromElementSpaces() {
		return fromElementSpaces;
	}

	public void addFromElementSpace(SqmFromElementSpace space) {

	}
	public SqmFromElementSpace makeFromElementSpace() {
		final SqmFromElementSpace space = new SqmFromElementSpace( this );
		fromElementSpaces.add( space );
		return space;
	}
}
