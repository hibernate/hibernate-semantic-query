/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.descriptor.java.spi.TemporalJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.orm.type.spi.ColumnMapping;
import org.hibernate.orm.type.spi.TemporalType;

/**
 * @author Steve Ebersole
 */
public class TemporalTypeImpl extends BasicTypeImpl implements TemporalType {
	private final javax.persistence.TemporalType precision;

	public TemporalTypeImpl(
			String typeName,
			TemporalJavaTypeDescriptor javaDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator,
			ColumnMapping columnMapping,
			javax.persistence.TemporalType precision) {
		super( typeName, javaDescriptor, mutabilityPlan, comparator, columnMapping );
		this.precision = precision;
	}

	public TemporalTypeImpl(
			String typeName,
			TemporalJavaTypeDescriptor javaTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator,
			SqlTypeDescriptor sqlTypeDescriptor,
			javax.persistence.TemporalType precision) {
		super( typeName, javaTypeDescriptor, mutabilityPlan, comparator, sqlTypeDescriptor );
		this.precision = precision;
	}

	@Override
	public TemporalJavaTypeDescriptor getJavaTypeDescriptor() {
		return (TemporalJavaTypeDescriptor) super.getJavaTypeDescriptor();
	}

	@Override
	public javax.persistence.TemporalType getPrecision() {
		return precision;
	}

	@Override
	public ColumnMapping[] getColumnMappings() {
		return new ColumnMapping[0];
	}

	@Override
	public PersistenceType getPersistenceType() {
		return null;
	}
}
