/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.domain.EntityBinding;
import org.hibernate.sqm.query.JoinType;

/**
 * @author Steve Ebersole
 */
public class SqmCrossJoin extends AbstractFrom implements SqmJoin {

	public SqmCrossJoin(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityReference entityReference) {
		super(
				fromElementSpace,
				uid,
				alias,
				new EntityBinding( entityReference ),
				entityReference,
				new PropertyPath( null, entityReference.getEntityName() + "(" + alias + ")" )
		);
		getDomainReferenceBinding().injectFromElement( this );
	}

	@Override
	public EntityBinding getDomainReferenceBinding() {
		return (EntityBinding) super.getDomainReferenceBinding();
	}

	public String getEntityName() {
		return getDomainReferenceBinding().getBoundDomainReference().getEntityName();
	}

	@Override
	public JoinType getJoinType() {
		return JoinType.CROSS;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCrossJoinedFromElement( this );
	}
}
