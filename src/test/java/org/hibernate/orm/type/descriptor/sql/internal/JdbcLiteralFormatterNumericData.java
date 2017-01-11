/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.internal;

import org.hibernate.dialect.Dialect;

import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;

/**
 * @author Steve Ebersole
 */
public class JdbcLiteralFormatterNumericData extends BasicJdbcLiteralFormatter {
	private final Class unwrapJavaType;

	public JdbcLiteralFormatterNumericData(
			JavaTypeDescriptor javaTypeDescriptor,
			Class unwrapJavaType) {
		super( javaTypeDescriptor );
		this.unwrapJavaType = unwrapJavaType;
	}

	@Override
	public String toJdbcLiteral(Object value, Dialect dialect, WrapperOptions wrapperOptions) {
		return unwrap( value, unwrapJavaType, wrapperOptions ).toString();
	}
}
