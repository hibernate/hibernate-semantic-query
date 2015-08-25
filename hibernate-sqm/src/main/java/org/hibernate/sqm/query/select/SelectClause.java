/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.select;

import java.util.Map;

/**
 * @author Steve Ebersole
 */
public class SelectClause {
	private final boolean distinct;
	private final Selection selection;

	private Map<String, SelectItemExpression> stringSelectItemExpressionsByAlias;

	public SelectClause(Selection selection, boolean distinct) {
		this.selection = selection;
		this.distinct = distinct;
	}

	public SelectClause(Selection selection) {
		this( selection, false );
	}

	public boolean isDistinct() {
		return distinct;
	}

	public Selection getSelection() {
		return selection;
	}
}
