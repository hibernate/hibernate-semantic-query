/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.internal;

import java.util.Locale;

import org.hibernate.dialect.Dialect;

import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;

/**
 * @author Steve Ebersole
 */
public class JdbcLiteralFormatterCharacterData extends BasicJdbcLiteralFormatter {
	private final boolean isNationalized;

	public JdbcLiteralFormatterCharacterData(JavaTypeDescriptor javaTypeDescriptor) {
		this( javaTypeDescriptor, false );
	}

	public <T> JdbcLiteralFormatterCharacterData(JavaTypeDescriptor<T> javaTypeDescriptor, boolean isNationalized) {
		super( javaTypeDescriptor );
		this.isNationalized = isNationalized;
	}

	@Override
	public String toJdbcLiteral(Object value, Dialect dialect, WrapperOptions wrapperOptions) {
		if ( isNationalized ) {
			// is there a standardized form for n-string literals?  This is the SQL Server syntax for sure
			return String.format( Locale.ROOT, "n'%s'", value );
		}
		else {
			return String.format( Locale.ROOT, "'%s'", value );
		}
	}
}
