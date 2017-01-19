/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.embeddable.spi;

import org.hibernate.orm.persister.common.spi.ExpressableType;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.sqm.domain.SqmExpressableTypeEmbedded;

/**
 * Describes parts of the domain model that can be composite values.
 *
 * @author Steve Ebersole
 */
public interface EmbeddableReference extends SqmExpressableTypeEmbedded, ExpressableType {
	@Override
	EmbeddableContainer getSource();

	@Override
	EmbeddedType getExportedDomainType();

	EmbeddableMapper getEmbeddablePersister();
}
