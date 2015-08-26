/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
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
