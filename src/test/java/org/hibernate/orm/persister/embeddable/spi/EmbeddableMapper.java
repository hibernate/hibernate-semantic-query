/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.embeddable.spi;

import java.util.Collections;
import java.util.List;
import javax.persistence.metamodel.EmbeddableType;

import org.hibernate.mapping.Component;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.CompositeContainer;
import org.hibernate.orm.persister.common.spi.JoinColumnMapping;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.common.spi.OrmAttribute;
import org.hibernate.orm.persister.common.spi.OrmTypeExporter;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.sql.convert.spi.TableGroupProducer;
import org.hibernate.orm.type.spi.EmbeddedType;

/**
 * Mapping for an embedded value.  Represents a specific usage of an embeddable/composite
 *
 * @author Steve Ebersole
 */
public interface EmbeddableMapper<T>
		extends ManagedTypeImplementor<T>, OrmTypeExporter, CompositeContainer<T>, EmbeddableType<T> {
	void afterInitialization(
			Component embeddableBinding,
			PersisterCreationContext creationContext);

	String getRoleName();

	List<Column> collectColumns();

	@Override
	default PersistenceType getPersistenceType() {
		return PersistenceType.EMBEDDABLE;
	}

	@Override
	CompositeContainer<?> getSource();

	@Override
	EmbeddedType getOrmType();

	@Override
	default EmbeddedType getExportedDomainType() {
		return getOrmType();
	}

	@Override
	default String getTypeName() {
		return getOrmType().getJavaTypeDescriptor().getTypeName();
	}

	@Override
	@SuppressWarnings("unchecked")
	default Class<T> getJavaType() {
		return (Class<T>) getOrmType().getJavaTypeDescriptor().getJavaType();
	}

	@Override
	default boolean canCompositeContainCollections() {
		return getSource().canCompositeContainCollections();
	}

	@Override
	default TableGroupProducer resolveTableGroupProducer() {
		return getSource().resolveTableGroupProducer();
	}

	@Override
	default List<JoinColumnMapping> resolveJoinColumnMappings(OrmAttribute attribute) {
		return Collections.emptyList();
	}

	@Override
	default String asLoggableText() {
		return "EmbeddableMapper(" + getTypeName() + " [" + getRoleName() + "])";
	}
}
