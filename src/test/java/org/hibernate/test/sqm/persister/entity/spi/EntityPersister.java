/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.persister.entity.spi;

import org.hibernate.sqm.domain.type.DomainType;

import org.hibernate.test.sqm.persister.common.domain.spi.EntityReference;
import org.hibernate.test.sqm.type.spi.EntityType;

/**
 * @author Steve Ebersole
 */
public interface EntityPersister extends EntityReference{
	@Override
	EntityType getExportedDomainType();
}
