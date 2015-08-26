/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The semantic select clause.  Defined as a list of individual selections.
 *
 * @author Steve Ebersole
 */
public class SelectClause {
	private final boolean distinct;
	private List<Selection> selections;

	public SelectClause(boolean distinct) {
		this.distinct = distinct;
	}

	public SelectClause(boolean distinct, List<Selection> selections) {
		this.distinct = distinct;
		this.selections = selections;
	}

	public SelectClause(boolean distinct, Selection... selections) {
		this( distinct, Arrays.asList( selections ) );
	}

	public boolean isDistinct() {
		return distinct;
	}

	public List<Selection> getSelections() {
		if ( selections == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( selections );
		}
	}

	public void addSelection(Selection selection) {
		if ( selections == null ) {
			selections = new ArrayList<Selection>();
		}
		selections.add( selection );
	}
}
