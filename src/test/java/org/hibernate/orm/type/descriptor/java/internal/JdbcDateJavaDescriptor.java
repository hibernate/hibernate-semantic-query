/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.persistence.TemporalType;

import org.hibernate.HibernateException;

import org.hibernate.orm.type.descriptor.java.spi.AbstractBasicTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutableMutabilityPlan;
import org.hibernate.orm.type.descriptor.java.spi.TemporalJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.orm.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link java.sql.Date} handling.
 *
 * @author Steve Ebersole
 */
public class JdbcDateJavaDescriptor extends AbstractBasicTypeDescriptor<Date> implements TemporalJavaTypeDescriptor<Date> {
	public static final JdbcDateJavaDescriptor INSTANCE = new JdbcDateJavaDescriptor();
	public static final String DATE_FORMAT = "dd MMMM yyyy";

	public static class DateMutabilityPlan extends MutableMutabilityPlan<Date> {
		public static final DateMutabilityPlan INSTANCE = new DateMutabilityPlan();
		@Override
		public Date deepCopyNotNull(Date value) {
			return java.sql.Date.class.isInstance( value )
					? new java.sql.Date( value.getTime() )
					: new Date( value.getTime() );
		}
	}

	public JdbcDateJavaDescriptor() {
		super( Date.class, DateMutabilityPlan.INSTANCE );
	}
	@Override
	public String toString(Date value) {
		return new SimpleDateFormat( DATE_FORMAT ).format( value );
	}
	@Override
	public Date fromString(String string) {
		try {
			return new Date( new SimpleDateFormat(DATE_FORMAT).parse( string ).getTime() );
		}
		catch ( ParseException pe) {
			throw new HibernateException( "could not parse date string" + string, pe );
		}
	}

	@Override
	public boolean areEqual(Date one, Date another) {
		if ( one == another ) {
			return true;
		}
		if ( one == null || another == null ) {
			return false;
		}

		if ( one.getTime() == another.getTime() ) {
			return true;
		}

		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime( one );
		calendar2.setTime( another );

		return calendar1.get( Calendar.MONTH ) == calendar2.get( Calendar.MONTH )
				&& calendar1.get( Calendar.DAY_OF_MONTH ) == calendar2.get( Calendar.DAY_OF_MONTH )
				&& calendar1.get( Calendar.YEAR ) == calendar2.get( Calendar.YEAR );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		return context.getTypeConfiguration().getSqlTypeDescriptorRegistry().getDescriptor( Types.DATE );
	}

	@Override
	public int extractHashCode(Date value) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( value );
		int hashCode = 1;
		hashCode = 31 * hashCode + calendar.get( Calendar.MONTH );
		hashCode = 31 * hashCode + calendar.get( Calendar.DAY_OF_MONTH );
		hashCode = 31 * hashCode + calendar.get( Calendar.YEAR );
		return hashCode;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <X> X unwrap(Date value, Class<X> type, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( java.sql.Date.class.isAssignableFrom( type ) ) {
			final java.sql.Date rtn = java.sql.Date.class.isInstance( value )
					? ( java.sql.Date ) value
					: new java.sql.Date( value.getTime() );
			return (X) rtn;
		}
		if ( java.sql.Time.class.isAssignableFrom( type ) ) {
			final java.sql.Time rtn = java.sql.Time.class.isInstance( value )
					? ( java.sql.Time ) value
					: new java.sql.Time( value.getTime() );
			return (X) rtn;
		}
		if ( java.sql.Timestamp.class.isAssignableFrom( type ) ) {
			final java.sql.Timestamp rtn = java.sql.Timestamp.class.isInstance( value )
					? ( java.sql.Timestamp ) value
					: new java.sql.Timestamp( value.getTime() );
			return (X) rtn;
		}
		if ( Date.class.isAssignableFrom( type ) ) {
			return (X) value;
		}
		if ( Calendar.class.isAssignableFrom( type ) ) {
			final GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis( value.getTime() );
			return (X) cal;
		}
		if ( Long.class.isAssignableFrom( type ) ) {
			return (X) Long.valueOf( value.getTime() );
		}
		throw unknownUnwrap( type );
	}
	@Override
	public <X> Date wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( Date.class.isInstance( value ) ) {
			return (Date) value;
		}

		if ( Long.class.isInstance( value ) ) {
			return new java.sql.Date( ( (Long) value ).longValue() );
		}

		if ( Calendar.class.isInstance( value ) ) {
			return new java.sql.Date( ( (Calendar) value ).getTimeInMillis() );
		}

		if ( Date.class.isInstance( value ) ) {
			return new java.sql.Date( ( (Date) value ).getTime() );
		}

		throw unknownWrap( value.getClass() );
	}

	@Override
	public TemporalType getPrecision() {
		return TemporalType.DATE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> TemporalJavaTypeDescriptor<X> resolveTypeForPrecision(TemporalType precision, TypeConfiguration scope) {
		final TemporalJavaTypeDescriptor jdbcTimestampDescriptor = (TemporalJavaTypeDescriptor) scope.getJavaTypeDescriptorRegistry()
				.getDescriptor( java.sql.Timestamp.class );
		return jdbcTimestampDescriptor.resolveTypeForPrecision( precision, scope );
	}
}
