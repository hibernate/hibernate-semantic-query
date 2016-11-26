/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmStatementNonSelect;
import org.hibernate.sqm.query.from.SqmRoot;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmDmlStatement
		extends AbstractSqmStatement
		implements SqmStatementNonSelect, InFlightSqmSubQueryContainer {
	private SqmRoot entityFromElement;
	private List<SqmQuerySpecImpl> subQuerySpecs;

	@Override
	public SqmRoot getEntityFromElement() {
		return entityFromElement;
	}

	public void setEntityFromElement(SqmRoot entityFromElement) {
		this.entityFromElement = entityFromElement;
	}

	@Override
	public List<SqmQuerySpec> getSubQuerySpecs() {
		return subQuerySpecs.stream().collect( Collectors.toList() );
	}

	@Override
	public void addSubQuerySpec(SqmQuerySpecImpl subQuerySpec) {
		if ( subQuerySpec.getSubQueryContainerContainer() != this ) {
			throw new IllegalArgumentException(
					"Can only add subquery query-specs whose containing FromClauseContainer is the same as this SQM statement"
			);
		}

		if ( subQuerySpecs == null ) {
			subQuerySpecs = new ArrayList<>();
		}

		subQuerySpecs.add( subQuerySpec );
	}

}
