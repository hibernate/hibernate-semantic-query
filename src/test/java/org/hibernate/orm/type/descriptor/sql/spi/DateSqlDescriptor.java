/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.spi;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import javax.persistence.TemporalType;

import org.hibernate.orm.type.descriptor.java.spi.TemporalJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcValueBinder;
import org.hibernate.orm.type.descriptor.spi.JdbcValueExtractor;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.internal.JdbcLiteralFormatterTemporal;
import org.hibernate.orm.type.spi.JdbcLiteralFormatter;
import org.hibernate.orm.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link Types#DATE DATE} handling.
 *
 * @author Steve Ebersole
 */
public class DateSqlDescriptor implements TemporalSqlDescriptor {
	public static final DateSqlDescriptor INSTANCE = new DateSqlDescriptor();

	public DateSqlDescriptor() {
	}

	@Override
	public int getSqlType() {
		return Types.DATE;
	}

	@Override
	public boolean canBeRemapped() {
		return true;
	}

	@Override
	public TemporalJavaTypeDescriptor getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
		return (TemporalJavaTypeDescriptor) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( Date.class );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
		return new JdbcLiteralFormatterTemporal( (TemporalJavaTypeDescriptor) javaTypeDescriptor, TemporalType.DATE );
	}

	@Override
	public <X> JdbcValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new StandardJdbcValueBinder<X>( javaTypeDescriptor, this ) {
			@Override
			protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
				final Date date = javaTypeDescriptor.unwrap( value, Date.class, options );
				if ( value instanceof Calendar ) {
					st.setDate( index, date, (Calendar) value );
				}
				else {
					st.setDate( index, date );
				}
			}

			@Override
			protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
					throws SQLException {
				final Date date = javaTypeDescriptor.unwrap( value, Date.class, options );
				if ( value instanceof Calendar ) {
					st.setDate( name, date, (Calendar) value );
				}
				else {
					st.setDate( name, date );
				}
			}
		};
	}

	@Override
	public <X> JdbcValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new StandardJdbcValueExtractor<X>( javaTypeDescriptor, this ) {
			@Override
			protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( rs.getDate( name ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getDate( index ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getDate( name ), options );
			}
		};
	}
}
