/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.internal.InFlightSqmFromClauseContainer;
import org.hibernate.sqm.query.from.internal.SqmFromClauseImpl;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmSelectClause;

/**
 * @author Steve Ebersole
 */
public class SqmQuerySpecImpl implements SqmQuerySpec, InFlightSqmFromClauseContainer, InFlightSqmSubQueryContainer {
	private final InFlightSqmSubQueryContainer subQueryContainerContainer;
	private List<SqmQuerySpec> subQueries;

	private final SqmFromClauseImpl fromClause = new SqmFromClauseImpl( this );
	private SqmSelectClause selectClause;
	private SqmWhereClause whereClause;

	// todo : group-by + having

	public SqmQuerySpecImpl(InFlightSqmSubQueryContainer subQueryContainerContainer) {
		this.subQueryContainerContainer = subQueryContainerContainer;
		if ( this.subQueryContainerContainer != null ) {
			this.subQueryContainerContainer.addSubQuerySpec( this );
		}
	}

	public InFlightSqmSubQueryContainer getSubQueryContainerContainer() {
		return subQueryContainerContainer;
	}

	@Override
	public void addSubQuerySpec(SqmQuerySpecImpl subQuerySpec) {
		if ( subQuerySpec.getSubQueryContainerContainer() != this ) {
			throw new IllegalArgumentException(
					"Can only add subquery query-specs whose containing FromClauseContainer is the same as this SQM statement"
			);
		}

		if ( subQueries == null ) {
			subQueries = new ArrayList<>();
		}
		subQueries.add( subQuerySpec );
	}

	public void setSelectClause(SqmSelectClause selectClause) {
		this.selectClause = selectClause;
	}

	public void setWhereClause(SqmWhereClause whereClause) {
		this.whereClause = whereClause;
	}

	@Override
	public SqmFromClause getFromClause() {
		return fromClause;
	}

	@Override
	public SqmSelectClause getSelectClause() {
		return selectClause;
	}

	@Override
	public SqmWhereClause getWhereClause() {
		return whereClause;
	}

	@Override
	public List<SqmQuerySpec> getSubQuerySpecs() {
		if ( subQueries == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( subQueries );
		}
	}
}
