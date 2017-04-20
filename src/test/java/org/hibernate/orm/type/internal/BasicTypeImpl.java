/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.orm.type.spi.BasicType;
import org.hibernate.orm.type.spi.BasicTypeRegistry;
import org.hibernate.orm.type.spi.ColumnMapping;
import org.hibernate.orm.type.spi.JdbcLiteralFormatter;

/**
 * @author Steve Ebersole
 */
public class BasicTypeImpl<T> extends AbstractTypeImpl<T> implements BasicType<T> {
	private final ColumnMapping columnMapping;
	private final BasicTypeRegistry.Key registryKey;
	private final JdbcLiteralFormatter jdbcLiteralFormatter;

	@SuppressWarnings("unchecked")
	public BasicTypeImpl(
			String typeName,
			BasicJavaTypeDescriptor javaTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator,
			ColumnMapping columnMapping) {
		super( typeName, javaTypeDescriptor, mutabilityPlan, comparator );
		this.columnMapping = columnMapping;
		this.registryKey = BasicTypeRegistry.Key.from( javaTypeDescriptor, columnMapping.getSqlTypeDescriptor() );
		this.jdbcLiteralFormatter = columnMapping.getSqlTypeDescriptor().getJdbcLiteralFormatter( getJavaTypeDescriptor() );
	}

	@SuppressWarnings("unchecked")
	public BasicTypeImpl(
			String typeName,
			BasicJavaTypeDescriptor javaTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator,
			SqlTypeDescriptor sqlTypeDescriptor) {
		super( typeName, javaTypeDescriptor, mutabilityPlan, comparator );
		this.columnMapping = new ColumnMapping( sqlTypeDescriptor );
		this.registryKey = BasicTypeRegistry.Key.from( javaTypeDescriptor, sqlTypeDescriptor );
		this.jdbcLiteralFormatter = sqlTypeDescriptor == null ? null : sqlTypeDescriptor.getJdbcLiteralFormatter( getJavaTypeDescriptor() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public BasicJavaTypeDescriptor<T> getJavaTypeDescriptor() {
		return (BasicJavaTypeDescriptor) super.getJavaTypeDescriptor();
	}

	@Override
	public JdbcLiteralFormatter getJdbcLiteralFormatter() {
		return jdbcLiteralFormatter;
	}

	@Override
	public BasicTypeRegistry.Key getRegistryKey() {
		return registryKey;
	}

	@Override
	public Class<T> getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}

	@Override
	public String asLoggableText() {
		return "BasicType(" + getTypeName() + ")";
	}

	@Override
	public ColumnMapping getColumnMapping() {
		return columnMapping;
	}

	@Override
	public BasicType<T> getExportedDomainType() {
		return this;
	}
}
