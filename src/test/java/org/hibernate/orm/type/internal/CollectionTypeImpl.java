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
import org.hibernate.orm.type.spi.CollectionType;
import org.hibernate.orm.type.spi.ColumnMapping;
import org.hibernate.orm.type.spi.JdbcLiteralFormatter;

/**
 * @author Steve Ebersole
 */
public class CollectionTypeImpl extends AbstractTypeImpl implements CollectionType {
	public CollectionTypeImpl(
			String roleName,
			JavaTypeDescriptor javaTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		super( roleName, javaTypeDescriptor, mutabilityPlan, comparator );
	}

	@Override
	public String asLoggableText() {
		return null;
	}

	@Override
	public String getTypeName() {
		return null;
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return null;
	}

	@Override
	public ColumnMapping[] getColumnMappings() {
		return new ColumnMapping[0];
	}

	@Override
	public MutabilityPlan getMutabilityPlan() {
		return null;
	}

	@Override
	public Comparator getComparator() {
		return null;
	}

	@Override
	public JdbcLiteralFormatter getJdbcLiteralFormatter() {
		return null;
	}

	@Override
	public Class getJavaType() {
		return null;
	}
}
