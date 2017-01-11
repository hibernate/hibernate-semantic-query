/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.spi;

import org.hibernate.orm.persister.common.spi.OrmNavigable;
import org.hibernate.orm.type.spi.Type;
import org.hibernate.sqm.domain.SqmEntityIdentifier;

/**
 * Base information describing an identifier
 *
 * @author Steve Ebersole
 */
public interface IdentifierDescriptor<E> extends OrmNavigable, SqmEntityIdentifier {
	@Override
	EntityPersister getSource();

	Type getIdType();
	boolean hasSingleIdAttribute();

	String getReferableAttributeName();
}
