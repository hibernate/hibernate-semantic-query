/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.embeddable.spi;

import org.hibernate.orm.persister.common.spi.OrmNavigableSource;
import org.hibernate.orm.sql.convert.spi.TableGroupProducer;

/**
 * Contract for things that can contain composites.
 *
 * @author Steve Ebersole
 */
public interface EmbeddableContainer<T> extends OrmNavigableSource<T> {
	TableGroupProducer resolveTableGroupProducer();

	/**
	 * Holy alliteration Bat Man! :)
	 * <p/>
	 * Can the composites belonging to this container contain collections?  This
	 * would only be {@code false} in cases where the composite-container path
	 * is itself rooted in a collection element/index.
	 *
	 * @return {@code true} if the composites can contain collections; {@code false}
	 * otherwise.
	 */
	boolean canCompositeContainCollections();

	String getRolePrefix();
}
