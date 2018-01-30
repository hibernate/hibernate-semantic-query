/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.domain.SqmNavigableReference;
import org.hibernate.sqm.query.JoinType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractJoin extends AbstractFrom implements SqmJoin {
	private final JoinType joinType;

	public AbstractJoin(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			SqmNavigableReference bindableModelDescriptor,
			EntityDescriptor intrinsicSubclassIndicator,
			PropertyPath sourcePath,
			JoinType joinType) {
		super( fromElementSpace, uid, alias, bindableModelDescriptor, intrinsicSubclassIndicator, sourcePath );
		this.joinType = joinType;
	}

	@Override
	public JoinType getJoinType() {
		return joinType;
	}
}
