/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.internal;

import java.time.temporal.TemporalAccessor;
import javax.persistence.TemporalType;

import org.hibernate.dialect.Dialect;

import org.hibernate.orm.type.descriptor.internal.DateTimeUtils;
import org.hibernate.orm.type.descriptor.java.spi.TemporalJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;

/**
 * @author Steve Ebersole
 */
public class JdbcLiteralFormatterTemporal extends BasicJdbcLiteralFormatter {
	private final TemporalType precision;

	public JdbcLiteralFormatterTemporal(TemporalJavaTypeDescriptor javaTypeDescriptor, TemporalType precision) {
		super( javaTypeDescriptor );
		this.precision = precision;

		// todo : add some validation of combos between javaTypeDescrptor#getPrecision and precision - log warnings
	}

	@Override
	protected TemporalJavaTypeDescriptor getJavaTypeDescriptor() {
		return (TemporalJavaTypeDescriptor) super.getJavaTypeDescriptor();
	}

	@Override
	public String toJdbcLiteral(Object value, Dialect dialect, WrapperOptions wrapperOptions) {
		// for performance reasons, avoid conversions if we can
		if ( value instanceof java.util.Date ) {
			return DateTimeUtils.formatJdbcLiteralUsingPrecision(
					(java.util.Date) value,
					precision
			);
		}
		else if ( value instanceof java.util.Calendar ) {
			return DateTimeUtils.formatJdbcLiteralUsingPrecision(
					(java.util.Calendar) value,
					precision
			);
		}
		else if ( value instanceof TemporalAccessor ) {
			return DateTimeUtils.formatJdbcLiteralUsingPrecision(
					(TemporalAccessor) value,
					precision
			);
		}

		switch ( getJavaTypeDescriptor().getPrecision() ) {
			case DATE: {
				return DateTimeUtils.formatJdbcLiteralUsingPrecision(
						unwrap( value, java.sql.Date.class, wrapperOptions ),
						precision
				);
			}
			case TIME: {
				return DateTimeUtils.formatJdbcLiteralUsingPrecision(
						unwrap( value, java.sql.Time.class, wrapperOptions ),
						precision
				);
			}
			default: {
				return DateTimeUtils.formatJdbcLiteralUsingPrecision(
						unwrap( value, java.util.Date.class, wrapperOptions ),
						precision
				);
			}
		}
	}
}
