/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.internal;

import java.io.Serializable;

import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.type.descriptor.java.spi.EmbeddableJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.spi.ColumnMapping;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.orm.type.spi.JdbcLiteralFormatter;
import org.hibernate.orm.type.spi.ManagedType;
import org.hibernate.query.sqm.NotYetImplementedException;

/**
 * @author Steve Ebersole
 */
public class EmbeddedTypeImpl extends AbstractManagedType implements EmbeddedType {
	private final String roleName;

	public EmbeddedTypeImpl(
			ManagedType superType,
			String roleName,
			EmbeddableJavaTypeDescriptor javaTypeDescriptor) {
		super( superType, javaTypeDescriptor );
		this.roleName = roleName;
	}

	@Override
	public String getRoleName() {
		return roleName;
	}

	@Override
	public EmbeddedType getSuperType() {
		// for now...
		return null;
	}

	@Override
	public <T> EmbeddableMapper<T> getEmbeddableMapper() {
		return getTypeConfiguration().findEmbeddableMapper( roleName );
	}

	@Override
	public EmbeddableJavaTypeDescriptor getJavaTypeDescriptor() {
		return (EmbeddableJavaTypeDescriptor) super.getJavaTypeDescriptor();
	}

	@Override
	public ColumnMapping[] getColumnMappings() {
		throw new NotYetImplementedException(  );
	}

	@Override
	public JdbcLiteralFormatter getJdbcLiteralFormatter() {
		// we could potentially render a literal *if* all composed attributes also define a literal formatter
		return null;
	}

	@Override
	public String asLoggableText() {
		return "Embeddable(" + getTypeName() + ")";
	}

	private static class EmbeddedMutabilityPlan implements MutabilityPlan {
		/**
		 * Singleton access
		 */
		public static final EmbeddedMutabilityPlan INSTANCE = new EmbeddedMutabilityPlan();

		@Override
		public boolean isMutable() {
			return true;
		}

		@Override
		public Object deepCopy(Object value) {
			throw new UnsupportedOperationException( "Illegal call to EmbeddedType's MutabilityPlan" );
		}

		@Override
		public Serializable disassemble(Object value) {
			return null;
		}

		@Override
		public Object assemble(Serializable cached) {
			return null;
		}
	}
}
