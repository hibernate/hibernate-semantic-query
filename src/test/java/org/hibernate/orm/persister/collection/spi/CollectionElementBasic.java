/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.collection.spi;

import org.hibernate.orm.type.spi.BasicType;
import org.hibernate.query.sqm.domain.SqmPluralAttributeElementBasic;

/**
 * @author Steve Ebersole
 */
public interface CollectionElementBasic extends CollectionElement, SqmPluralAttributeElementBasic {
	@Override
	BasicType getExportedDomainType();
}
