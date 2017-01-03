/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.persister.common.domain.spi;

import org.hibernate.test.sqm.domain.PluralAttributeElementImpl;
import org.hibernate.test.sqm.type.internal.AbstractSingularAttribute;
import org.hibernate.test.sqm.persister.entity.spi.EntityPersister;
import org.hibernate.test.sqm.type.spi.EntityType;

/**
 * See {@link org.hibernate.test.sqm.persister}...
 *
 * This would polymorphically represent a reference to:<ul>
 *     <li>{@link EntityPersister} itself</li>
 *     <li>{@link AbstractSingularAttribute}</li>
 *     <li>{@link PluralAttributeElementImpl} </li>
 * </ul>
 *
 * @author Steve Ebersole
 */
public interface EntityReference extends Navigable {
	EntityPersister getEntityPersister();

	@Override
	EntityType getExportedDomainType();
}
