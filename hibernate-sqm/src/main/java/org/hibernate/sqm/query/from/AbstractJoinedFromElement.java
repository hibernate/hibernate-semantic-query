/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.query.JoinType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractJoinedFromElement extends AbstractFromElement implements JoinedFromElement {
	private final JoinType joinType;

	public AbstractJoinedFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			Bindable bindableModelDescriptor,
			JoinType joinType) {
		super( fromElementSpace, alias, bindableModelDescriptor );
		this.joinType = joinType;
	}

	@Override
	public JoinType getJoinType() {
		return joinType;
	}
}
