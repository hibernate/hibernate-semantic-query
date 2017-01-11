/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.IdentifiableJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.spi.IdentifiableType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractIdentifiableType extends AbstractManagedType implements IdentifiableType {
	public AbstractIdentifiableType(IdentifiableType superType, IdentifiableJavaTypeDescriptor javaTypeDescriptor) {
		super( superType, javaTypeDescriptor );
	}

	public AbstractIdentifiableType(
			IdentifiableType superType,
			IdentifiableJavaTypeDescriptor javaTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		super( superType, javaTypeDescriptor, mutabilityPlan, comparator );
	}

	@Override
	public IdentifiableJavaTypeDescriptor getJavaTypeDescriptor() {
		return (IdentifiableJavaTypeDescriptor) super.getJavaTypeDescriptor();
	}

	@Override
	public IdentifiableType getSuperType() {
		return (IdentifiableType) super.getSuperType();
	}
}
