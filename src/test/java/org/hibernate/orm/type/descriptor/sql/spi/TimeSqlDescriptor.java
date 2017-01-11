/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.spi;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
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
 * Descriptor for {@link Types#TIME TIME} handling.
 *
 * @author Steve Ebersole
 */
public class TimeSqlDescriptor implements TemporalSqlDescriptor {
	public static final TimeSqlDescriptor INSTANCE = new TimeSqlDescriptor();

	public TimeSqlDescriptor() {
	}

	@Override
	public int getSqlType() {
		return Types.TIME;
	}

	@Override
	public boolean canBeRemapped() {
		return true;
	}

	@Override
	public TemporalJavaTypeDescriptor getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
		return (TemporalJavaTypeDescriptor) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( Time.class );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
		return new JdbcLiteralFormatterTemporal( (TemporalJavaTypeDescriptor) javaTypeDescriptor, TemporalType.TIME );
	}

	@Override
	public <X> JdbcValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new StandardJdbcValueBinder<X>( javaTypeDescriptor, this ) {
			@Override
			protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
				final Time time = javaTypeDescriptor.unwrap( value, Time.class, options );
				if ( value instanceof Calendar ) {
					st.setTime( index, time, (Calendar) value );
				}
				else {
					st.setTime( index, time );
				}
			}

			@Override
			protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
					throws SQLException {
				final Time time = javaTypeDescriptor.unwrap( value, Time.class, options );
				if ( value instanceof Calendar ) {
					st.setTime( name, time, (Calendar) value );
				}
				else {
					st.setTime( name, time );
				}
			}
		};
	}

	@Override
	public <X> JdbcValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new StandardJdbcValueExtractor<X>( javaTypeDescriptor, this ) {
			@Override
			protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( rs.getTime( name ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getTime( index ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getTime( name ), options );
			}
		};
	}
}
