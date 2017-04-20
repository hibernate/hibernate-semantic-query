/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.persister.collection.spi;

import java.util.List;

import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.OrmNavigable;
import org.hibernate.orm.persister.common.spi.OrmTypeExporter;
import org.hibernate.orm.type.spi.Type;
import org.hibernate.query.sqm.domain.SqmPluralAttributeElement;

/**
 * @author Steve Ebersole
 */
public interface CollectionElement<O extends Type>
		extends OrmTypeExporter, SqmPluralAttributeElement, OrmNavigable {
	List<Column> getColumns();

}
