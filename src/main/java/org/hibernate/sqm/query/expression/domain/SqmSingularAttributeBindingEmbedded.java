/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.SqmSingularAttributeEmbedded;
import org.hibernate.sqm.domain.SqmSingularAttribute;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * @author Steve Ebersole
 */
public class SqmSingularAttributeBindingEmbedded extends AbstractSqmSingularAttributeBinding implements
		SqmEmbeddableTypedBinding {
	public SqmSingularAttributeBindingEmbedded(
			SqmNavigableSourceBinding domainReferenceBinding,
			SqmSingularAttribute boundNavigable) {
		super( domainReferenceBinding, boundNavigable );
	}

	public SqmSingularAttributeBindingEmbedded(SqmAttributeJoin fromElement) {
		super( fromElement );
	}

	@Override
	public SqmSingularAttributeEmbedded getBoundNavigable() {
		return (SqmSingularAttributeEmbedded) super.getBoundNavigable();
	}
}
