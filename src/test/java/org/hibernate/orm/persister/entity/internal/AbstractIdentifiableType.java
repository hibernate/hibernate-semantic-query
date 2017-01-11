/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.orm.persister.OrmTypeHelper;
import org.hibernate.orm.persister.common.internal.AbstractManagedType;
import org.hibernate.orm.persister.common.internal.OrmSingularAttributeBasic;
import org.hibernate.orm.persister.common.internal.OrmSingularAttributeEmbedded;
import org.hibernate.orm.persister.common.internal.PersisterHelper;
import org.hibernate.orm.persister.common.spi.OrmNavigable;
import org.hibernate.orm.persister.common.spi.OrmSingularAttribute;
import org.hibernate.orm.persister.entity.spi.EntityHierarchy;
import org.hibernate.orm.persister.entity.spi.IdentifiableTypeImplementor;
import org.hibernate.orm.persister.entity.spi.IdentifierDescriptorNonAggregatedEmbedded;
import org.hibernate.orm.persister.entity.spi.IdentifierDescriptorSingleAttribute;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractIdentifiableType<T> extends AbstractManagedType<T> implements IdentifiableTypeImplementor<T> {
	private static final Logger log = Logger.getLogger( AbstractIdentifiableType.class );

	private EntityHierarchy hierarchy;
	private List<IdentifiableTypeImplementor> subclassTypes;

	public AbstractIdentifiableType(
			JavaTypeDescriptor typeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		super( typeDescriptor, mutabilityPlan, comparator );
	}

	@Override
	public EntityHierarchy getHierarchy() {
		return hierarchy;
	}

	void injectEntityHierarchy(EntityHierarchy hierarchy) {
		log.debugf(
				"Injecting EntityHierarchy [%s] in IdentifiableType [%]; was [%s]",
				hierarchy,
				this,
				this.hierarchy
		);
		this.hierarchy = hierarchy;
	}

	@Override
	public OrmNavigable findNavigable(String navigableName) {
		if ( hierarchy.getIdentifierDescriptor().getReferableAttributeName().equals( navigableName ) ) {
			return hierarchy.getIdentifierDescriptor();
		}

		if ( "<id>".equals( navigableName ) ) {
			return hierarchy.getIdentifierDescriptor();
		}

		OrmNavigable navigable = super.findNavigable( navigableName );
		if ( navigable == null && "id".equals( navigableName ) ) {
			return hierarchy.getIdentifierDescriptor();
		}

		return navigable;
	}

	@Override
	public String getRolePrefix() {
		return getTypeName();
	}

	@Override
	public Type<?> getIdType() {
		if ( getHierarchy().getIdentifierDescriptor() instanceof IdentifierDescriptorSingleAttribute ) {
			final OrmSingularAttribute idAttribute = ( (IdentifierDescriptorSingleAttribute) getHierarchy().getIdentifierDescriptor() )
					.getIdAttribute();
			if ( idAttribute instanceof OrmSingularAttributeBasic ) {
				return ( (OrmSingularAttributeBasic) idAttribute ).getOrmType();
			}
			else if ( idAttribute instanceof OrmSingularAttributeEmbedded ) {
				return ( (OrmSingularAttributeEmbedded) idAttribute ).getEmbeddablePersister();
			}
			else {
				throw new IllegalStateException( "Expected BASIC or EMBEDDED attribute type for identifier" );
			}
		}
		return null;
	}

	@Override
	public OrmSingularAttribute getId(Class type) {
		return findIdAttribute( type );
	}

	private OrmSingularAttribute findIdAttribute(Class type) {
		if ( IdentifierDescriptorSingleAttribute.class.isInstance( hierarchy.getIdentifierDescriptor() ) ) {
			final OrmSingularAttribute idAttribute = ( (IdentifierDescriptorSingleAttribute) hierarchy.getIdentifierDescriptor() )
					.getIdAttribute();

			if ( idAttribute != null )  {
				checkAttributeType( idAttribute, type );
			}

			return idAttribute;
		}

		throw new IllegalArgumentException( "Illegal call to get id attribute on IdentifiableType with IdClass" );
	}

	@Override
	public OrmSingularAttribute getDeclaredId(Class type) {
		final OrmSingularAttribute idAttribute = findIdAttribute( type );
		if ( idAttribute.getSource() != this ) {
			throw new IllegalArgumentException(
					String.format(
							Locale.ROOT,
							"Id is declared on [%s], not [%s]",
							idAttribute.getSource().asLoggableText(),
							asLoggableText()
					)
			);
		}
		return idAttribute;
	}

	@Override
	public OrmSingularAttribute getVersion(Class type) {
		return findIdAttribute( type );
	}

	private OrmSingularAttribute findVersionAttribute(Class type) {
		if ( hierarchy.getVersionAttribute() == null ) {
			throw new IllegalArgumentException( "Entity hierarchy does not define version attribute" );
		}

		checkAttributeType( hierarchy.getVersionAttribute(), type );

		return hierarchy.getVersionAttribute();
	}

	@Override
	public OrmSingularAttribute getDeclaredVersion(Class type) {
		final OrmSingularAttribute versionAttribute = findVersionAttribute( type );
		if ( versionAttribute.getSource() != this ) {
			throw new IllegalArgumentException(
					String.format(
							Locale.ROOT,
							"Version is declared on [%s], not [%s]",
							versionAttribute.getSource().asLoggableText(),
							asLoggableText()
					)
			);
		}
		return versionAttribute;
	}

	@Override
	public IdentifiableTypeImplementor getSuperType() {
		return (IdentifiableTypeImplementor) super.getSuperType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return IdentifierDescriptorSingleAttribute.class.isInstance( hierarchy.getIdentifierDescriptor() );
	}

	@Override
	public boolean hasVersionAttribute() {
		return hierarchy.getVersionAttribute() != null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<SingularAttribute<? super T, ?>> getIdClassAttributes() {
		if ( hierarchy.getIdentifierDescriptor() instanceof IdentifierDescriptorNonAggregatedEmbedded ) {
			return ( (IdentifierDescriptorNonAggregatedEmbedded<?,? super T>) hierarchy.getIdentifierDescriptor() ).getIdentifierAttributes()
					.stream()
					.collect( Collectors.toSet() );
		}

		throw new IllegalArgumentException( "No IdClass" );
	}

	@Override
	public void finishInitialization(
			EntityHierarchy entityHierarchy,
			IdentifiableTypeImplementor<? super T> superType,
			PersistentClass mappingDescriptor,
			PersisterCreationContext creationContext) {
		injectEntityHierarchy( entityHierarchy );
		injectSuperTypeDescriptor( superType );

		setTypeConfiguration( creationContext.getTypeConfiguration() );

		if ( superType instanceof AbstractIdentifiableType ) {
			( (AbstractIdentifiableType) superType ).registerIdentifiableTypeSubclass( this );
		}

		final Iterator itr = mappingDescriptor.getDeclaredPropertyIterator();
		while ( itr.hasNext() ) {
			final Property mappingProperty = (Property) itr.next();
			log.tracef( "Starting building of Entity attribute : %s#%s", mappingDescriptor.getEntityName(), mappingProperty.getName() );

			addAttribute(
					PersisterHelper.INSTANCE.buildAttribute(
							creationContext,
							this,
							mappingProperty.getValue(),
							mappingProperty.getName(),
							OrmTypeHelper.convert(
									creationContext,
									this,
									mappingProperty.getName(),
									mappingProperty.getValue(),
									creationContext.getTypeConfiguration()
							),
							//PersisterHelper.makeValues( ... )
							Collections.emptyList()
					)
			);
		}
	}

	private void registerIdentifiableTypeSubclass(IdentifiableTypeImplementor subclassType) {
		if ( subclassTypes == null ) {
			subclassTypes = new ArrayList<>();
		}
		subclassTypes.add( subclassType );
	}
}
