/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.query.expression.domain.EntityBinding;

/**
 * @author Steve Ebersole
 */
public class SqmRoot extends AbstractFrom {
	public SqmRoot(
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
				alias
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
	public EntityReference getIntrinsicSubclassIndicator() {
		// a root FromElement cannot indicate a subclass intrinsically (as part of its declaration)
		return null;
	}

	@Override
	public String toString() {
		return getEntityName() + " as " + getIdentificationVariable();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitRootEntityFromElement( this );
	}
}
