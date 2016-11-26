/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.from.internal;

import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmJoin;
import org.hibernate.sqm.query.from.SqmRoot;

/**
 * Mimics a FromElementSpace in relation to a DML statement.  There will only be one
 * FromElementSpace for the DML Statement
 *
 * @author Steve Ebersole
 */
public class DmlFromElementSpace extends FromElementSpace {
	DmlFromElementSpace(DmlFromClause fromClause) {
		super( fromClause );
	}

	@Override
	public void setRoot(SqmRoot root) {
		super.setRoot( root );
		dmlFromClause().getDmlStatement().setEntityFromElement( root );
	}

	private DmlFromClause dmlFromClause() {
		return (DmlFromClause) getFromClause();
	}

	@Override
	public List<SqmJoin> getJoins() {
		return Collections.emptyList();
	}

	@Override
	public void addJoin(SqmJoin join) {
		throw new ParsingException( "DML from-clause cannot define joins" );
	}
}
