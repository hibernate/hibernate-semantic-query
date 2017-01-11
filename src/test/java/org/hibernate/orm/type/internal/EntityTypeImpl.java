/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.internal;

import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.type.descriptor.java.spi.EntityJavaTypeDescriptor;
import org.hibernate.orm.type.spi.ColumnMapping;
import org.hibernate.orm.type.spi.EntityType;
import org.hibernate.orm.type.spi.IdentifiableType;
import org.hibernate.orm.type.spi.JdbcLiteralFormatter;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.sqm.NotYetImplementedException;

/**
 * @author Steve Ebersole
 */
public class EntityTypeImpl extends AbstractIdentifiableType implements EntityType {
	public EntityTypeImpl(
			IdentifiableType superType,
			EntityJavaTypeDescriptor javaTypeDescriptor,
			TypeConfiguration typeConfiguration) {
		super( superType, javaTypeDescriptor );

		setTypeConfiguration( typeConfiguration );
	}


	@Override
	public EntityJavaTypeDescriptor getJavaTypeDescriptor() {
		return (EntityJavaTypeDescriptor) super.getJavaTypeDescriptor();
	}

	@Override
	public ColumnMapping[] getColumnMappings() {
		throw new NotYetImplementedException(  );
	}

	@Override
	public JdbcLiteralFormatter getJdbcLiteralFormatter() {
		return null;
	}

	@Override
	public EntityPersister getEntityPersister() {
		return getTypeConfiguration().findEntityPersister( getEntityName() );
	}

	@Override
	public String getEntityName() {
		return getJavaTypeDescriptor().getEntityName();
	}

	@Override
	public String getJpaEntityName() {
		return getJavaTypeDescriptor().getJpaEntityName();
	}

	@Override
	public String asLoggableText() {
		return "Entity(" + getEntityName() + ")";
	}
}
