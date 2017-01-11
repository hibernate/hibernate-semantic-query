/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.spi;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.internal.util.compare.ComparableComparator;
import org.hibernate.internal.util.compare.EqualsHelper;

/**
 * Abstract adapter for Java type descriptors.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractTypeDescriptor<T> implements JavaTypeDescriptor<T>, Serializable {
	private final Class<T> javaType;
	private final MutabilityPlan<T> mutabilityPlan;
	private final Comparator<T> comparator;

	/**
	 * Initialize a type descriptor for the given type.  Assumed immutable.
	 *
	 * @param javaType The Java type.
	 *
	 * @see #AbstractTypeDescriptor(Class, MutabilityPlan)
	 */
	@SuppressWarnings({ "unchecked" })
	protected AbstractTypeDescriptor(Class<T> javaType) {
		this( javaType, (MutabilityPlan<T>) ImmutableMutabilityPlan.INSTANCE );
	}

	/**
	 * Initialize a type descriptor for the given type.  Assumed immutable.
	 *
	 * @param javaType The Java type.
	 * @param mutabilityPlan The plan for handling mutability aspects of the java type.
	 */
	@SuppressWarnings({ "unchecked" })
	protected AbstractTypeDescriptor(Class<T> javaType, MutabilityPlan<T> mutabilityPlan) {
		this(
				javaType,
				mutabilityPlan,
				Comparable.class.isAssignableFrom( javaType )
						? (Comparator<T>) ComparableComparator.INSTANCE
						: null
		);
	}

	public AbstractTypeDescriptor(Class<T> javaType, MutabilityPlan<T> mutabilityPlan, Comparator<T> comparator) {
		this.javaType = javaType;
		this.mutabilityPlan = mutabilityPlan;
		this.comparator = comparator;
	}

	@Override
	public MutabilityPlan<T> getMutabilityPlan() {
		return mutabilityPlan;
	}

	@Override
	public Class<T> getJavaType() {
		return javaType;
	}

	@Override
	public int extractHashCode(T value) {
		return value.hashCode();
	}

	@Override
	public boolean areEqual(T one, T another) {
		return EqualsHelper.equals( one, another );
	}

	@Override
	public Comparator<T> getComparator() {
		return comparator;
	}

	@Override
	public String extractLoggableRepresentation(T value) {
		return (value == null) ? "null" : value.toString();
	}

	protected HibernateException unknownUnwrap(Class conversionType) {
		throw new HibernateException(
				"Unknown unwrap conversion requested: " + getTypeName() + " to " + conversionType.getName()
		);
	}

	protected HibernateException unknownWrap(Class conversionType) {
		throw new HibernateException(
				"Unknown wrap conversion requested: " + conversionType.getName() + " to " + getTypeName()
		);
	}

}
