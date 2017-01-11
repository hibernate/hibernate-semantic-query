/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.ManagedJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.orm.type.spi.TypeConfigurationAware;

import org.jboss.logging.Logger;

/**
 * Base support for all ManagedType implementations.managed types, which is the JPA term for commonality between entity, embeddable and
 * "mapped superclass" types.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractManagedJavaDescriptor
		extends AbstractJavaDescriptor
		implements ManagedJavaTypeDescriptor, TypeConfigurationAware {
	private static final Logger log = Logger.getLogger( AbstractManagedJavaDescriptor.class );

	private final ManagedJavaTypeDescriptor superTypeDescriptor;

	private TypeConfiguration typeConfiguration;

	public AbstractManagedJavaDescriptor(
			String typeName,
			Class javaType,
			ManagedJavaTypeDescriptor superTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		super( typeName, javaType, mutabilityPlan, comparator );
		this.superTypeDescriptor = superTypeDescriptor;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	@Override
	public void setTypeConfiguration(TypeConfiguration typeConfiguration) {
		this.typeConfiguration = typeConfiguration;
	}

	@Override
	public ManagedJavaTypeDescriptor getSuperType() {
		return superTypeDescriptor;
	}
}
