/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.persister.common.spi;

import javax.persistence.metamodel.PluralAttribute;

import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.sqm.domain.SqmPluralAttribute;

/**
 * @author Steve Ebersole
 */
public interface PluralAttribute<O,C,E>
		extends OrmAttribute<O,C>, JoinableOrmAttribute<O,C>, OrmTypeExporter,
		javax.persistence.metamodel.PluralAttribute<O,C,E>, SqmPluralAttribute {
	@Override
	org.hibernate.orm.type.spi.CollectionType getOrmType();

	CollectionPersister<O,C,E> getCollectionPersister();
}
