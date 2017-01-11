/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.spi;

import org.hibernate.orm.persister.common.spi.ExpressableType;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;

import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public interface BasicType<T> extends Type<T>, SqmDomainTypeBasic, ExpressableType<T>, javax.persistence.metamodel.BasicType<T> {
	BasicTypeRegistry.Key getRegistryKey();

	@Override
	BasicJavaTypeDescriptor<T> getJavaTypeDescriptor();

	@Override
	default ColumnMapping[] getColumnMappings() {
		return new ColumnMapping[] { getColumnMapping() };
	}

	ColumnMapping getColumnMapping();

	@Override
	default PersistenceType getPersistenceType() {
		return PersistenceType.BASIC;
	}
}
