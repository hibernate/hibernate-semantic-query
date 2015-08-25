/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractJoinedFromElement extends AbstractFromElement implements JoinedFromElement {
	private final JoinType joinType;

	public AbstractJoinedFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			TypeDescriptor typeDescriptor,
			JoinType joinType) {
		super( fromElementSpace, alias, typeDescriptor );
		this.joinType = joinType;
	}

	@Override
	public JoinType getJoinType() {
		return joinType;
	}
}
