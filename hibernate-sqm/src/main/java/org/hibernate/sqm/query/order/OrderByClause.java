/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class OrderByClause {
	private List<SortSpecification> sortSpecifications;

	public OrderByClause() {
	}

	public OrderByClause addSortSpecification(SortSpecification sortSpecification) {
		if ( sortSpecifications == null ) {
			sortSpecifications = new ArrayList<SortSpecification>();
		}
		sortSpecifications.add( sortSpecification );
		return this;
	}

	public OrderByClause addSortSpecification(Expression expression) {
		addSortSpecification( new SortSpecification( expression ) );
		return this;
	}

	public List<SortSpecification> getSortSpecifications() {
		if ( sortSpecifications == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( sortSpecifications );
		}
	}
}
