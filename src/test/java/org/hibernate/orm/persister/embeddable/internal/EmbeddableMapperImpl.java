/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.embeddable.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.orm.persister.OrmTypeHelper;
import org.hibernate.orm.persister.common.internal.AbstractManagedType;
import org.hibernate.orm.persister.common.internal.PersisterHelper;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.CompositeContainer;
import org.hibernate.orm.persister.common.spi.JoinColumnMapping;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.common.spi.OrmAttribute;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.sql.convert.spi.TableGroupProducer;
import org.hibernate.orm.type.descriptor.java.internal.EmbeddableJavaTypeDescriptorImpl;
import org.hibernate.orm.type.descriptor.java.spi.EmbeddableJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptorRegistry;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.internal.EmbeddedTypeImpl;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.orm.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class EmbeddableMapperImpl<T>
		extends AbstractManagedType<T>
		implements EmbeddableMapper<T> {
	private final CompositeContainer compositeContainer;
	private final String locaName;
	private final String roleName;
	private final EmbeddedType ormType;
	private final List<Column> allColumns;

	@SuppressWarnings("unchecked")
	public EmbeddableMapperImpl(
			PersisterCreationContext creationContext,
			ManagedTypeImplementor superTypeDescriptor,
			CompositeContainer compositeContainer,
			String locaName,
			Component embeddedMapping,
			MutabilityPlan mutabilityPlan,
			Comparator comparator,
			List<Column> allColumns) {
		super( resolveJtd( creationContext, embeddedMapping ) );

		this.compositeContainer = compositeContainer;
		this.locaName = locaName;
		this.roleName = compositeContainer.getRolePrefix() + '.' + locaName;
		this.allColumns = allColumns;

		this.ormType = new EmbeddedTypeImpl( null, roleName, getJavaTypeDescriptor() );

		setTypeConfiguration( creationContext.getTypeConfiguration() );
		ormType.setTypeConfiguration( creationContext.getTypeConfiguration() );
	}

	private static EmbeddableJavaTypeDescriptor resolveJtd(
			PersisterCreationContext creationContext,
			Component embeddedMapping) {
		JavaTypeDescriptorRegistry jtdr = creationContext.getTypeConfiguration().getJavaTypeDescriptorRegistry();
		EmbeddableJavaTypeDescriptor jtd = (EmbeddableJavaTypeDescriptor) jtdr.getDescriptor( embeddedMapping.getType().getName() );
		if ( jtd == null ) {
			jtd = new EmbeddableJavaTypeDescriptorImpl(
					embeddedMapping.getType().getName(),
					embeddedMapping.getType().getReturnedClass(),
					null,
					null,
					null
			);
			jtdr.addDescriptor( jtd );
		}
		return jtd;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EmbeddableJavaTypeDescriptor getJavaTypeDescriptor() {
		return (EmbeddableJavaTypeDescriptor) super.getJavaTypeDescriptor();
	}

	@Override
	public void afterInitialization(
			Component embeddableBinding,
			PersisterCreationContext creationContext) {
		final Iterator<Property> mappingPropertyItr = embeddableBinding.getPropertyIterator();
		while ( mappingPropertyItr.hasNext() ) {
			final Property mappingProperty = mappingPropertyItr.next();

			// todo : Columns
			final List<Column> columns = Collections.emptyList();

			final OrmAttribute attribute = PersisterHelper.INSTANCE.buildAttribute(
					creationContext,
					this,
					embeddableBinding,
					mappingProperty.getName(),
					OrmTypeHelper.convert(
							creationContext,
							this,
							locaName,
							mappingProperty.getValue(),
							creationContext.getTypeConfiguration()
					),
					columns
			);
			addAttribute( attribute );

		}
	}

	@Override
	public String getRoleName() {
		return roleName;
	}

	@Override
	public EmbeddedType getOrmType() {
		return ormType;
	}

	@Override
	public TableGroupProducer resolveTableGroupProducer() {
		return compositeContainer.resolveTableGroupProducer();
	}

	@Override
	public boolean canCompositeContainCollections() {
		return compositeContainer.canCompositeContainCollections();
	}

	@Override
	public String getRolePrefix() {
		return getRoleName();
	}

	public List<Column> collectColumns() {
		return allColumns;
	}

	@Override
	public CompositeContainer getSource() {
		return compositeContainer;
	}

	@Override
	public String getNavigableName() {
		return locaName;
	}

	@Override
	public String getTypeName() {
		return ormType.getJavaTypeDescriptor().getTypeName();
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.EMBEDDABLE;
	}

	@Override
	public EmbeddedType getExportedDomainType() {
		return ormType;
	}

	@Override
	public String asLoggableText() {
		return "EmdeddablePersister(" + roleName + " [" + getTypeName() + "])";
	}

	@Override
	public List<JoinColumnMapping> resolveJoinColumnMappings(OrmAttribute attribute) {
		return Collections.emptyList();
	}
}
