/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.IdentifiableJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.ManagedJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractIdentifiableJavaDescriptor
		extends AbstractManagedJavaDescriptor
		implements IdentifiableJavaTypeDescriptor {

	public AbstractIdentifiableJavaDescriptor(
			String typeName,
			Class javaType,
			ManagedJavaTypeDescriptor superTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		super( typeName, javaType, superTypeDescriptor, mutabilityPlan, comparator );
	}

	@Override
	public IdentifiableJavaTypeDescriptor getSuperType() {
		return (IdentifiableJavaTypeDescriptor) super.getSuperType();
	}
}
