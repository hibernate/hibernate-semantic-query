/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.spi;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.sqm.domain.type.SqmDomainType;

/**
 * @author Steve Ebersole
 */
public interface Type<T> extends SqmDomainType<T> {
	String getTypeName();

	JavaTypeDescriptor getJavaTypeDescriptor();

	/**
	 * Describes the columns mapping for this Type.
	 *
	 * @return The column mapping for this Type
	 */
	ColumnMapping[] getColumnMappings();


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// these generally represent overrides of the delegated
	// 		JavaTypeDescriptor's values

	MutabilityPlan getMutabilityPlan();

	Comparator getComparator();

	JdbcLiteralFormatter getJdbcLiteralFormatter();
}
