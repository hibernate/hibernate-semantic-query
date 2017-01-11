/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractJoin extends AbstractFrom implements SqmJoin {
	private final JoinType joinType;

	public AbstractJoin(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			SqmNavigableBinding navigableBinding,
			SqmExpressableTypeEntity intrinsicSubclassIndicator,
			JoinType joinType) {
		super( fromElementSpace, uid, alias, navigableBinding, intrinsicSubclassIndicator );
		this.joinType = joinType;
	}

	@Override
	public JoinType getJoinType() {
		return joinType;
	}
}
