/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.collection.spi;

import java.util.List;

import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.OrmNavigableSource;
import org.hibernate.orm.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractOrmPluralAttributeElement<O extends Type> implements OrmPluralAttributeElement {
	private final CollectionPersister persister;
	private final O ormType;
	private final List<Column> columns;

	public AbstractOrmPluralAttributeElement(
			CollectionPersister persister,
			O ormType,
			List<Column> columns) {
		this.persister = persister;
		this.ormType = ormType;
		this.columns = columns;
	}

	@Override
	public CollectionPersister getSource() {
		return persister;
	}

	@Override
	public String getNavigableName() {
		return "{element}";
	}

	@Override
	public O getOrmType() {
		return ormType;
	}

	@Override
	public Class getJavaType() {
		return null;
	}

	@Override
	public String getTypeName() {
		return getOrmType().getTypeName();
	}

	@Override
	public O getExportedDomainType() {
		return getOrmType();
	}

	@Override
	public List<Column> getColumns() {
		return columns;
	}
}
