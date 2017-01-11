/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmSingularAttributeEntity;
import org.hibernate.sqm.domain.SqmSingularAttribute;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * @author Steve Ebersole
 */
public class SqmSingularAttributeBindingEntity extends AbstractSqmSingularAttributeBinding implements
		SqmEntityTypedBinding {
	public SqmSingularAttributeBindingEntity(
			SqmNavigableSourceBinding domainReferenceBinding,
			SqmSingularAttribute boundNavigable) {
		super( domainReferenceBinding, boundNavigable );
	}

	public SqmSingularAttributeBindingEntity(SqmAttributeJoin fromElement) {
		super( fromElement );
	}

	@Override
	public SqmSingularAttributeEntity getBoundNavigable() {
		return (SqmSingularAttributeEntity) super.getBoundNavigable();
	}

	@Override
	public SqmExpressableTypeEntity getExpressionType() {
		return (SqmExpressableTypeEntity) super.getExpressionType();
	}

	@Override
	public SqmExpressableTypeEntity getInferableType() {
		return getExpressionType();
	}
}
