/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.collection.internal;

import java.util.Collections;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.metamodel.Type;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.IndexedCollection;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Value;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.collection.spi.CollectionElement;
import org.hibernate.orm.persister.collection.spi.CollectionId;
import org.hibernate.orm.persister.collection.spi.CollectionIndex;
import org.hibernate.orm.persister.collection.spi.CollectionKey;
import org.hibernate.orm.persister.common.internal.PersisterHelper;
import org.hibernate.orm.persister.common.spi.AbstractOrmAttribute;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.JoinColumnMapping;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.common.spi.OrmAttribute;
import org.hibernate.orm.persister.common.spi.OrmNavigable;
import org.hibernate.orm.persister.common.spi.OrmNavigableSource;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.type.descriptor.java.internal.CollectionJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.ListJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.MapJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.SetJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptorRegistry;
import org.hibernate.orm.type.internal.CollectionTypeImpl;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.sqm.NotYetImplementedException;
import org.hibernate.type.BasicType;
import org.hibernate.type.EntityType;

import static org.hibernate.orm.persister.OrmTypeHelper.convertBasic;
import static org.hibernate.orm.persister.OrmTypeHelper.convertComposite;
import static org.hibernate.orm.persister.OrmTypeHelper.convertEntity;

/**
 * @author Steve Ebersole
 */
public class CollectionPersisterImpl<O,C,E> extends AbstractOrmAttribute<O,C> implements CollectionPersister<O,C,E> {
	public static final String INDEX_NAVIGABLE_NAME = "{index}";
	public static final String ELEMENT_NAVIGABLE_NAME = "{element}";

	private final OrmNavigableSource source;
	private final String localName;
	private final CollectionClassification collectionClassification;

	private final org.hibernate.orm.type.spi.CollectionType collectionType;

	private final CollectionKey foreignKeyDescriptor;
	private CollectionId idDescriptor;
	private CollectionElement elementDescriptor;
	private CollectionIndex indexDescriptor;

	private org.hibernate.orm.persister.common.spi.Table separateCollectionTable;

	private AttributeConverter indexAttributeConverter;
	private AttributeConverter elementAttributeConverter;

	// TODO: encapsulate the protected instance variables!

	private final String role;

	private TypeConfiguration typeConfiguration;

	public CollectionPersisterImpl(
			Collection collectionBinding,
			ManagedTypeImplementor source,
			String localName,
			CollectionRegionAccessStrategy collectionCaching,
			PersisterCreationContext creationContext) {
		super( source, localName, PropertyAccess.DUMMY );
		this.source = source;
		this.localName = localName;
		this.role = source.getNavigableName() + '.' + this.localName;
		this.collectionClassification = PersisterHelper.interpretCollectionClassification( collectionBinding );
		this.foreignKeyDescriptor = new CollectionKey( this );

		this.typeConfiguration = creationContext.getTypeConfiguration();

		this.collectionType = new CollectionTypeImpl(
				role,
				resolveCollectionJtd( creationContext, collectionClassification ),
				null,
				null
		);

		if ( collectionBinding instanceof IndexedCollection ) {
			final Value indexValueMapping = ( (IndexedCollection) collectionBinding ).getIndex();
			if ( indexValueMapping instanceof SimpleValue ) {
				final SimpleValue simpleIndexValueMapping = (SimpleValue) indexValueMapping;
//				indexAttributeConverter = simpleIndexValueMapping.getAttributeConverterDescriptor().getAttributeConverter();
			}
		}

		final Value elementValueMapping = collectionBinding.getElement();
		if ( elementValueMapping instanceof SimpleValue ) {
			final SimpleValue simpleElementValueMapping = (SimpleValue) elementValueMapping;
//			elementAttributeConverter = simpleElementValueMapping.getAttributeConverterDescriptor().getAttributeConverter();
		}
	}

	@SuppressWarnings("unchecked")
	private static JavaTypeDescriptor resolveCollectionJtd(
			org.hibernate.orm.persister.spi.PersisterCreationContext creationContext,
			CollectionClassification collectionClassification) {
		final JavaTypeDescriptorRegistry jtdr = creationContext.getTypeConfiguration().getJavaTypeDescriptorRegistry();
		JavaTypeDescriptor jtd;

		if ( collectionClassification == CollectionClassification.BAG ) {
			jtd = jtdr.getDescriptor( java.util.Collection.class );
			if ( jtd == null ) {
				// todo : make one
				jtd = new CollectionJavaDescriptor();
				// register the JavaTypeDescriptor we just created.
				//jtdr.addDescriptor( jtd );
			}
		}
		else if ( collectionClassification == CollectionClassification.LIST ) {
			jtd = jtdr.getDescriptor( java.util.List.class );
			if ( jtd == null ) {
				// todo : make one
				jtd = new ListJavaDescriptor();
				// register it with the registry for later
				jtdr.addDescriptor( jtd );
			}
		}
		else if ( collectionClassification == CollectionClassification.MAP ) {
			// todo : what about SortedMap variants?
			jtd = jtdr.getDescriptor( java.util.Map.class );
			if ( jtd == null ) {
				// make one
				jtd = new MapJavaDescriptor();
				// register it with the registry for later
				jtdr.addDescriptor( jtd );
			}
		}
		else if ( collectionClassification == CollectionClassification.SET ) {
			// todo : what about SortedSet variants?
			jtd = jtdr.getDescriptor( java.util.Set.class );
			if ( jtd == null ) {
				// todo : make one
				jtd = new SetJavaDescriptor();
				// register it with the registry for later
				jtdr.addDescriptor( jtd );
			}
		}
		else {
			throw new HibernateException( "Could not resolve Java 'collection type' for : " + collectionClassification );
		}

		return jtd;
	}

	@Override
	public void finishInitialization(Collection collectionBinding, PersisterCreationContext creationContext) {
		if ( collectionBinding instanceof IdentifierCollection ) {
			this.idDescriptor = new CollectionId(
					convertBasic(
							(BasicType) ( (IdentifierCollection) collectionBinding ).getIdentifier().getType(),
							creationContext.getTypeConfiguration()
					),
					// for now we do not need the id generator...
					//( (IdentifierCollection) collectionBinding ).getIdentifier().createIdentifierGenerator( ... )
					null
			);
		}
		else {
			this.idDescriptor = null;
		}

		if ( collectionBinding instanceof IndexedCollection ) {
			final IndexedCollection indexedCollection = (IndexedCollection) collectionBinding;
			final List<Column> columns = Collections.emptyList();

			if ( indexedCollection.getIndex().getType().isAnyType() ) {
				throw new NotYetImplementedException(  );
			}
			else if ( indexedCollection.getIndex().getType().isComponentType() ) {
				this.indexDescriptor = new CollectionIndexEmbeddedImpl(
						this,
						convertComposite(
								creationContext,
								CollectionIndex.NAVIGABLE_NAME,
								(Component) indexedCollection.getIndex(),
								this,
								typeConfiguration
						),
						columns
				);
			}
			else if ( indexedCollection.getIndex().getType().isEntityType() ) {
				this.indexDescriptor = new CollectionIndexEntityImpl(
						this,
						convertEntity(
								creationContext,
								(EntityType) indexedCollection.getIndex().getType(),
								typeConfiguration
						),
						columns
				);
			}
			else {
				this.indexDescriptor = new CollectionIndexBasicImpl(
						this,
						convertBasic( (BasicType) indexedCollection.getIndex().getType(), typeConfiguration ),
						columns
				);
			}
		}
		else {
			this.indexDescriptor = null;
		}


		final List<Column> elementColumns = Collections.emptyList();
		if ( collectionBinding.getElement().getType().isAnyType() ) {
			throw new NotYetImplementedException(  );
		}
		else if ( collectionBinding.getElement().getType().isComponentType() ) {
			this.elementDescriptor = new CollectionElementEmbeddedImpl(
					this,
					convertComposite(
							creationContext,
							CollectionIndex.NAVIGABLE_NAME,
							(Component) collectionBinding.getElement(),
							this,
							typeConfiguration
					),
					elementColumns
			);
		}
		else if ( collectionBinding.getElement().getType().isEntityType() ) {
			this.elementDescriptor = new CollectionElementEntityImpl(
					this,
					convertEntity(
							creationContext,
							(EntityType) collectionBinding.getElement().getType(),
							typeConfiguration
					),
					elementColumns
			);
		}
		else {
			this.elementDescriptor = new CollectionElementBasicImpl(
					this,
					convertBasic( (BasicType) collectionBinding.getElement().getType(), typeConfiguration ),
					elementColumns
			);
		}
	}

	@Override
	public String getRoleName() {
		return role;
	}

	@Override
	public CollectionKey getForeignKeyDescriptor() {
		return foreignKeyDescriptor;
	}

	@Override
	public CollectionId getIdDescriptor() {
		return idDescriptor;
	}

	@Override
	public CollectionIndex getIndexReference() {
		return indexDescriptor;
	}

	@Override
	public CollectionClassification getCollectionClassification() {
		return collectionClassification;
	}

	@Override
	public CollectionElement getElementReference() {
		return elementDescriptor;
	}

	@Override
	public String getRole() {
		return getRoleName();
	}

	@Override
	public CollectionType getCollectionType() {
		return collectionClassification.toJpaClassification();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Type<E> getElementType() {
		return getElementReference();
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		switch ( getElementReference().getClassification() ) {
			case MANY_TO_MANY: {
				return PersistentAttributeType.MANY_TO_MANY;
			}
			case ONE_TO_MANY: {
				return PersistentAttributeType.ONE_TO_MANY;
			}
			default: {
				return PersistentAttributeType.ELEMENT_COLLECTION;
			}
		}
	}

	@Override
	public boolean isAssociation() {
		return false;
	}

	@Override
	public boolean isCollection() {
		return true;
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.PLURAL_ATTRIBUTE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<E> getBindableJavaType() {
		return getElementReference().getJavaType();
	}

	@Override
	public String asLoggableText() {
		return null;
	}

	@Override
	public String getTypeName() {
		return null;
	}

	@Override
	public OrmNavigable findNavigable(String navigableName) {
		// only valid navigable-names are:
		//		1) "{index}"
		//		2) "{element}"

		if ( INDEX_NAVIGABLE_NAME.equals( navigableName ) ) {
			return getIndexReference();
		}
		else if ( ELEMENT_NAVIGABLE_NAME.equals( navigableName ) ) {
			return getElementReference();
		}

		throw new HibernateException(
				"Unrecognized navigable-name [" + navigableName +
						"]; relative to plural attribute only {index} and {element} are valid names"
		);
	}

	@Override
	public List<JoinColumnMapping> resolveJoinColumnMappings(OrmAttribute attribute) {
		return Collections.emptyList();
	}

	@Override
	public org.hibernate.orm.type.spi.CollectionType getOrmType() {
		return collectionType;
	}

	@Override
	public CollectionPersister<O, C, E> getCollectionPersister() {
		return null;
	}

	@Override
	public List<JoinColumnMapping> getJoinColumnMappings() {
		return null;
	}

	@Override
	public boolean canCompositeContainCollections() {
		return false;
	}

	@Override
	public String getRolePrefix() {
		return null;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return null;
	}
}
