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

/**
 * @author Steve Ebersole
 */
public class SqmRoot extends AbstractFrom {
	public SqmRoot(
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
	public EntityDescriptor getIntrinsicSubclassIndicator() {
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
