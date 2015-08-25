/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.phase1;

import org.hibernate.sqm.query.from.FromClause;

/**
 * @author Andrea Boriero
 * @author Steve Ebersole
 */
public class FromClauseStackNode {
	private FromClause fromClause;
	private FromClauseStackNode parentNode;

	public FromClauseStackNode(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	public FromClauseStackNode(FromClause fromClause, FromClauseStackNode parentNode) {
		this.fromClause = fromClause;
		this.parentNode = parentNode;
	}

	public FromClause getFromClause() {
		return fromClause;
	}

	public FromClauseStackNode getParentNode() {
		return parentNode;
	}

	public boolean hasParent() {
		return parentNode != null;
	}
}
