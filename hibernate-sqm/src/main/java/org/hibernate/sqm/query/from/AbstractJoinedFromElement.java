/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
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
