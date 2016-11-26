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
import org.hibernate.sqm.query.from.SqmFromClauseContainer;
import org.hibernate.sqm.query.internal.AbstractSqmDmlStatement;

/**
 * Mimics a FromClause in relation to a DML statement, exporting the
 * entity that is the target of the DML as the FromClause root FromElement
 *
 * @author Steve Ebersole
 */
public class DmlFromClause extends SqmFromClauseImpl {
	private final DmlFromElementSpace fromElementSpace = new DmlFromElementSpace( this );
	private final AbstractSqmDmlStatement dmlStatement;

	public DmlFromClause(AbstractSqmDmlStatement dmlStatement, SqmFromClauseContainer fromClauseContainer) {
		super( fromClauseContainer );
		this.dmlStatement = dmlStatement;
	}

	public AbstractSqmDmlStatement getDmlStatement() {
		return dmlStatement;
	}

	public DmlFromElementSpace getFromElementSpace() {
		return fromElementSpace;
	}

	@Override
	public List<FromElementSpace> getFromElementSpaces() {
		return Collections.singletonList( fromElementSpace );
	}

	@Override
	public void addFromElementSpace(FromElementSpace space) {
		throw new ParsingException( "DML from-clause cannot have additional FromElementSpaces" );
	}

	@Override
	public FromElementSpace makeFromElementSpace() {
		throw new ParsingException( "DML from-clause cannot have additional FromElementSpaces" );
	}
}
