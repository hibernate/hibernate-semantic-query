/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.collection.internal;

import java.util.List;

import org.hibernate.orm.persister.collection.spi.AbstractCollectionIndex;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.collection.spi.CollectionIndexBasic;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.type.spi.BasicType;

/**
 * @author Steve Ebersole
 */
public class CollectionIndexBasicImpl
		extends AbstractCollectionIndex<BasicType>
		implements CollectionIndexBasic {

	public CollectionIndexBasicImpl(
			CollectionPersister persister,
			BasicType ormType,
			List<Column> columns) {
		super( persister, ormType, columns );
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.BASIC;
	}
}
