/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.spi.Type;

/**
 * Abstract support for all Type implementations.  Mainly adds "type name" handling.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractTypeImpl<T> implements Type<T> {
	private final String typeName;
	private final JavaTypeDescriptor javaTypeDescriptor;
	private final MutabilityPlan mutabilityPlan;
	private final Comparator comparator;

	public AbstractTypeImpl(
			String typeName,
			JavaTypeDescriptor javaTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		this.typeName = typeName;
		this.javaTypeDescriptor = javaTypeDescriptor;
		this.mutabilityPlan = mutabilityPlan == null ? javaTypeDescriptor.getMutabilityPlan() : mutabilityPlan;
		this.comparator = comparator == null ? javaTypeDescriptor.getComparator() : comparator;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return javaTypeDescriptor;
	}

	@Override
	public MutabilityPlan getMutabilityPlan() {
		return mutabilityPlan;
	}

	@Override
	public Comparator getComparator() {
		return comparator;
	}
}
