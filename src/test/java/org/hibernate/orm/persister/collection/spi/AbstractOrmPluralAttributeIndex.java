/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.persister.collection.spi;

import java.util.List;

import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractOrmPluralAttributeIndex<O extends Type> implements OrmPluralAttributeIndex {
	private final CollectionPersister persister;
	private final O ormType;
	private final List<Column> columns;

	public AbstractOrmPluralAttributeIndex(CollectionPersister persister, O ormType, List<Column> columns) {
		this.persister = persister;
		this.ormType = ormType;
		this.columns = columns;
	}
	@Override
	public CollectionPersister getSource() {
		return persister;
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
	public Class getJavaType() {
		return getOrmType().getJavaType();
	}

	@Override
	public String getNavigableName() {
		return "{index}";
	}

	@Override
	public O getOrmType() {
		return ormType;
	}

	@Override
	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "PluralAttributeIndex(" + persister.getRole() + " [" + getOrmType().getTypeName() + "])";
	}
}