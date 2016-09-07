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

import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * The semantic select clause.  Defined as a list of individual selections.
 *
 * @author Steve Ebersole
 */
public class SqmSelectClause implements SqmAliasedExpressionContainer<SqmSelection> {
	private final boolean distinct;
	private List<SqmSelection> selections;

	public SqmSelectClause(boolean distinct) {
		this.distinct = distinct;
	}

	public SqmSelectClause(boolean distinct, List<SqmSelection> selections) {
		this.distinct = distinct;
		this.selections = selections;
	}

	public SqmSelectClause(boolean distinct, SqmSelection... selections) {
		this( distinct, Arrays.asList( selections ) );
	}

	public boolean isDistinct() {
		return distinct;
	}

	public List<SqmSelection> getSelections() {
		if ( selections == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( selections );
		}
	}

	public void addSelection(SqmSelection selection) {
		if ( selections == null ) {
			selections = new ArrayList<>();
		}
		selections.add( selection );
	}

	@Override
	public SqmSelection add(SqmExpression expression, String alias) {
		final SqmSelection selection = new SqmSelection( expression, alias );
		addSelection( selection );
		return selection;
	}

	@Override
	public void add(SqmSelection aliasExpression) {
		addSelection( aliasExpression );
	}
}
