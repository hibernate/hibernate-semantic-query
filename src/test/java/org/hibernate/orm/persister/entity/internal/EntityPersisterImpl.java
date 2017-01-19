/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.internal;

import java.util.List;

import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.orm.persister.common.spi.JoinColumnMapping;
import org.hibernate.orm.persister.common.spi.OrmAttribute;
import org.hibernate.orm.persister.common.spi.OrmNavigableSource;
import org.hibernate.orm.persister.entity.spi.EntityHierarchy;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.entity.spi.IdentifiableTypeImplementor;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.sql.convert.spi.TableGroupProducer;
import org.hibernate.orm.type.descriptor.java.internal.EntityJavaTypeDescriptorImpl;
import org.hibernate.orm.type.descriptor.java.spi.EntityJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptorRegistry;
import org.hibernate.sqm.domain.type.SqmDomainTypeEntity;

/**
 * @author Steve Ebersole
 */
public class EntityPersisterImpl<T>
		extends AbstractIdentifiableType<T>
		implements EntityPersister<T>, SqmDomainTypeEntity<T> {

	private final String entityName;
	private final String jpaEntityName;

	public EntityPersisterImpl(
			PersistentClass mappingDescriptor,
			EntityRegionAccessStrategy entityCaching,
			NaturalIdRegionAccessStrategy naturalIdCaching,
			PersisterCreationContext creationContext) {
		super( resolveEntityJavaTypeDescriptor( creationContext, mappingDescriptor), null, null );
		this.entityName = mappingDescriptor.getEntityName();
		this.jpaEntityName = mappingDescriptor.getJpaEntityName();

		setTypeConfiguration( creationContext.getTypeConfiguration() );
	}

	private static EntityJavaTypeDescriptor resolveEntityJavaTypeDescriptor(
			PersisterCreationContext creationContext,
			PersistentClass mappingDescriptor) {
		final JavaTypeDescriptorRegistry jtdr = creationContext.getTypeConfiguration().getJavaTypeDescriptorRegistry();
		EntityJavaTypeDescriptor jtd = (EntityJavaTypeDescriptor) jtdr.getDescriptor( mappingDescriptor.getEntityName() );
		if ( jtd == null ) {
			jtd = new EntityJavaTypeDescriptorImpl(
					mappingDescriptor.getClassName(),
					mappingDescriptor.getEntityName(),
					mappingDescriptor.getMappedClass(),
					null,
					null,
					null
			);
		}
		return jtd;
	}

	@Override
	public void finishInitialization(
			EntityHierarchy entityHierarchy,
			IdentifiableTypeImplementor<? super T> superType,
			PersistentClass mappingDescriptor,
			PersisterCreationContext creationContext) {
		super.finishInitialization( entityHierarchy, superType, mappingDescriptor, creationContext );
	}

	@Override
	public String asLoggableText() {
		return "EntityPersister(" + getEntityName() + ")";
	}

	@Override
	public EntityPersister<T> getEntityPersister() {
		return this;
	}

	@Override
	public EntityJavaTypeDescriptor<T> getJavaTypeDescriptor() {
		return (EntityJavaTypeDescriptor<T>) super.getJavaTypeDescriptor();
	}

	@Override
	public SqmDomainTypeEntity getExportedDomainType() {
		return this;
	}

	@Override
	public OrmNavigableSource getSource() {
		return null;
	}

	@Override
	public String getNavigableName() {
		return getEntityName();
	}

	@Override
	public String getEntityName() {
		return entityName;
	}

	@Override
	public String getTypeName() {
		return getJavaTypeDescriptor().getTypeName();
	}

	@Override
	public String getJpaEntityName() {
		return jpaEntityName;
	}

	@Override
	public String getName() {
		return getEntityName();
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.ENTITY_TYPE;
	}

	@Override
	public Class<T> getBindableJavaType() {
		return getJavaType();
	}

	@Override
	public TableGroupProducer resolveTableGroupProducer() {
		return this;
	}

	@Override
	public List<JoinColumnMapping> resolveJoinColumnMappings(OrmAttribute attribute) {
		return null;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.ENTITY;
	}

	@Override
	public Class<T> getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}
}
