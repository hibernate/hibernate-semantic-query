/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.spi;

import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.orm.persister.common.spi.OrmNavigableSource;
import org.hibernate.orm.sql.convert.spi.TableGroupProducer;
import org.hibernate.orm.type.descriptor.java.spi.EntityJavaTypeDescriptor;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.query.sqm.domain.type.SqmDomainType;

/**
 * @author Steve Ebersole
 */
public interface EntityPersister<T>
		extends EntityReference<T>, IdentifiableTypeImplementor<T>, javax.persistence.metamodel.EntityType<T>,
		SqmDomainType<T>, TableGroupProducer {

	/**
	 * Unless a custom {@link org.hibernate.persister.spi.PersisterFactory} is used, it is expected
	 * that implementations of EntityPersister define a constructor accepting the following arguments:<ol>
	 *     <li>
	 *         {@link org.hibernate.mapping.PersistentClass} - describes the metadata about the entity
	 *         to be handled by the persister
	 *     </li>
	 *     <li>
	 *         {@link EntityRegionAccessStrategy} - the second level caching strategy for this entity
	 *     </li>
	 *     <li>
	 *         {@link NaturalIdRegionAccessStrategy} - the second level caching strategy for the natural-id
	 *         defined for this entity, if one
	 *     </li>
	 *     <li>
	 *         {@link org.hibernate.persister.spi.PersisterCreationContext} - access to additional
	 *         information useful while constructing the persister.
	 *     </li>
	 * </ol>
	 */
	Class[] CONSTRUCTOR_SIGNATURE = new Class[] {
			PersistentClass.class,
			EntityRegionAccessStrategy.class,
			NaturalIdRegionAccessStrategy.class,
			PersisterCreationContext.class
	};

	@Override
	IdentifiableTypeImplementor<? super T> getSuperType();

	EntityJavaTypeDescriptor<T> getJavaTypeDescriptor();

	@Override
	OrmNavigableSource getSource();

	@Override
	default EntityPersister<T> getEntityPersister() {
		return this;
	}
}
