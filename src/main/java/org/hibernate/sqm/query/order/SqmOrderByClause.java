/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmOrderByClause {
	private List<SqmSortSpecification> sortSpecifications;

	public SqmOrderByClause() {
	}

	public SqmOrderByClause addSortSpecification(SqmSortSpecification sortSpecification) {
		if ( sortSpecifications == null ) {
			sortSpecifications = new ArrayList<SqmSortSpecification>();
		}
		sortSpecifications.add( sortSpecification );
		return this;
	}

	public SqmOrderByClause addSortSpecification(SqmExpression expression) {
		addSortSpecification( new SqmSortSpecification( expression ) );
		return this;
	}

	public List<SqmSortSpecification> getSortSpecifications() {
		if ( sortSpecifications == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( sortSpecifications );
		}
	}
}
