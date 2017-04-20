/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.spi;

import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.type.descriptor.java.spi.EntityJavaTypeDescriptor;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEntity;

/**
 * @author Steve Ebersole
 */
public interface EntityType extends IdentifiableType, SqmDomainTypeEntity {
	EntityPersister getEntityPersister();

	@Override
	EntityJavaTypeDescriptor getJavaTypeDescriptor();

	String getTypeName();
	String getEntityName();
	String getJpaEntityName();
}
