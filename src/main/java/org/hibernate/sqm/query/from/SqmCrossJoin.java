/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.domain.EntityReference;
import org.hibernate.sqm.query.JoinType;

/**
 * @author Steve Ebersole
 */
public class SqmCrossJoin extends AbstractFrom implements SqmJoin {

	public SqmCrossJoin(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityDescriptor entityReference) {
		super(
				fromElementSpace,
				uid,
				alias,
				new EntityReference( entityReference ),
				entityReference,
				new PropertyPath( null, entityReference.getEntityName() + "(" + alias + ")" )
		);
		getDomainReferenceBinding().injectFromElement( this );
	}

	@Override
	public EntityReference getDomainReferenceBinding() {
		return (EntityReference) super.getDomainReferenceBinding();
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
