/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.from.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmFromClauseContainer;

/**
 * @author Steve Ebersole
 */
public class SqmFromClauseImpl implements SqmFromClause {
	private final SqmFromClauseContainer container;

	private List<FromElementSpace> fromElementSpaces = new ArrayList<>();

	public SqmFromClauseImpl(SqmFromClauseContainer container) {
		this.container = container;
	}

	@Override
	public SqmFromClauseContainer getContainer() {
		return container;
	}

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
