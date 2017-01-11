/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.internal;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.persistence.TemporalType;

import org.hibernate.orm.type.descriptor.java.spi.TemporalJavaTypeDescriptor;
import org.hibernate.type.OffsetDateTimeType;

import org.hibernate.orm.type.descriptor.java.spi.AbstractBasicTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.ImmutableMutabilityPlan;
import org.hibernate.orm.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.orm.type.descriptor.spi.WrapperOptions;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.orm.type.spi.TypeConfiguration;

import org.jboss.logging.Logger;

/**
 * Java type descriptor for the LocalDateTime type.
 *
 * @author Steve Ebersole
 */
public class OffsetDateTimeJavaDescriptor extends AbstractBasicTypeDescriptor<OffsetDateTime> implements
		TemporalJavaTypeDescriptor<OffsetDateTime> {
	private static final Logger log = Logger.getLogger( OffsetDateTimeJavaDescriptor.class );

	/**
	 * Singleton access
	 */
	public static final OffsetDateTimeJavaDescriptor INSTANCE = new OffsetDateTimeJavaDescriptor();

	@SuppressWarnings("unchecked")
	public OffsetDateTimeJavaDescriptor() {
		super( OffsetDateTime.class, ImmutableMutabilityPlan.INSTANCE );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		return context.getTypeConfiguration().getSqlTypeDescriptorRegistry().getDescriptor( Types.TIMESTAMP );
	}

	@Override
	public String toString(OffsetDateTime value) {
		return OffsetDateTimeType.FORMATTER.format( value );
	}

	@Override
	public OffsetDateTime fromString(String string) {
		return OffsetDateTime.from( OffsetDateTimeType.FORMATTER.parse( string ) );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> X unwrap(OffsetDateTime offsetDateTime, Class<X> type, WrapperOptions options) {
		if ( offsetDateTime == null ) {
			return null;
		}

		if ( OffsetDateTime.class.isAssignableFrom( type ) ) {
			return (X) offsetDateTime;
		}

		if ( Calendar.class.isAssignableFrom( type ) ) {
			return (X) GregorianCalendar.from( offsetDateTime.toZonedDateTime() );
		}

		if ( Timestamp.class.isAssignableFrom( type ) ) {
			return (X) Timestamp.from( offsetDateTime.toInstant() );
		}

		if ( java.sql.Date.class.isAssignableFrom( type ) ) {
			return (X) java.sql.Date.from( offsetDateTime.toInstant() );
		}

		if ( java.sql.Time.class.isAssignableFrom( type ) ) {
			return (X) java.sql.Time.from( offsetDateTime.toInstant() );
		}

		if ( Date.class.isAssignableFrom( type ) ) {
			return (X) Date.from( offsetDateTime.toInstant() );
		}

		if ( Long.class.isAssignableFrom( type ) ) {
			return (X) Long.valueOf( offsetDateTime.toInstant().toEpochMilli() );
		}

		throw unknownUnwrap( type );
	}

	@Override
	public <X> OffsetDateTime wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}

		if ( OffsetDateTime.class.isInstance( value ) ) {
			return (OffsetDateTime) value;
		}

		if ( Timestamp.class.isInstance( value ) ) {
			final Timestamp ts = (Timestamp) value;
			return OffsetDateTime.ofInstant( ts.toInstant(), ZoneId.systemDefault() );
		}

		if ( Date.class.isInstance( value ) ) {
			final Date date = (Date) value;
			return OffsetDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault() );
		}

		if ( Long.class.isInstance( value ) ) {
			return OffsetDateTime.ofInstant( Instant.ofEpochMilli( (Long) value ), ZoneId.systemDefault() );
		}

		if ( Calendar.class.isInstance( value ) ) {
			final Calendar calendar = (Calendar) value;
			return OffsetDateTime.ofInstant( calendar.toInstant(), calendar.getTimeZone().toZoneId() );
		}

		throw unknownWrap( value.getClass() );
	}

	@Override
	public TemporalType getPrecision() {
		return TemporalType.TIMESTAMP;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> TemporalJavaTypeDescriptor<X> resolveTypeForPrecision(TemporalType precision, TypeConfiguration scope) {
		if ( precision == TemporalType.TIMESTAMP ) {
			return (TemporalJavaTypeDescriptor<X>) this;
		}
		if ( precision == TemporalType.DATE ) {
			log.debugf( "No JPA TemporalType#TIME Java representation for LocalDateTime, using LocalDateTime" );
			return (TemporalJavaTypeDescriptor<X>) this;
		}
		if ( precision == TemporalType.TIME ) {
			return (TemporalJavaTypeDescriptor<X>)scope.getJavaTypeDescriptorRegistry().getDescriptor( OffsetTime.class );
		}

		throw new IllegalArgumentException( "Unrecognized JPA TemporalType precision [" + precision + "]" );
	}
}
