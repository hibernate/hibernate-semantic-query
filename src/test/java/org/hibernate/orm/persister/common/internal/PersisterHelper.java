/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.persister.common.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.AttributeConverter;

import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Value;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.common.spi.AbstractOrmAttribute;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableContainer;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.common.spi.OrmAttribute;
import org.hibernate.orm.persister.common.spi.OrmNavigableSource;
import org.hibernate.orm.persister.common.spi.SingularAttribute;
import org.hibernate.orm.persister.common.spi.Table;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.orm.type.spi.AnyType;
import org.hibernate.orm.type.spi.BasicType;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.orm.type.spi.EntityType;
import org.hibernate.orm.type.spi.Type;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.sqm.NotYetImplementedException;
import org.hibernate.query.sqm.domain.SqmPluralAttributeElement.ElementClassification;
import org.hibernate.query.sqm.domain.SqmPluralAttribute.CollectionClassification;
import org.hibernate.query.sqm.domain.SqmSingularAttribute.SingularAttributeClassification;
import org.hibernate.query.sqm.tree.SqmPropertyPath;
import org.hibernate.type.ArrayType;
import org.hibernate.type.BagType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.IdentifierBagType;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.OrderedMapType;
import org.hibernate.type.OrderedSetType;
import org.hibernate.type.SetType;
import org.hibernate.type.SortedMapType;
import org.hibernate.type.SortedSetType;

/**
 * For now mainly a helper for reflection into stuff not exposed on the entity/collection persister
 * contracts
 *
 * @author Steve Ebersole
 */
public class PersisterHelper {
	/**
	 * Singleton access
	 */
	public static final PersisterHelper INSTANCE = new PersisterHelper();

	public Table getPropertyTable(EntityPersister persister, String attributeName, Table[] tables) {
		// todo : needed?
		throw new NotYetImplementedException(  );
	}

	public String[] getSubclassPropertyColumnExpressions(EntityPersister persister, int subclassPropertyNumber) {
		// todo : needed?
		throw new NotYetImplementedException(  );
	}

	public String[] getSubclassPropertyFormulaExpressions(EntityPersister persister, int subclassPropertyNumber) {
		// todo : needed?
		throw new NotYetImplementedException(  );
	}

	public static List<Column> makeValues(
			PersisterCreationContext creationContext,
			Table table,
			String[] columns,
			String[] formulas,
			// todo : SqlTypeDescriptor[] would be "best", but cannot currently resolve them.  callers need TypeConfiguration
			int[] jdbcTypeCodes) {
		assert columns != null;
		assert formulas == null || columns.length == formulas.length;
		assert jdbcTypeCodes != null;
		assert jdbcTypeCodes.length == columns.length;

		final List<Column> values = new ArrayList<>();

		for ( int i = 0; i < columns.length; i++ ) {
			if ( columns[i] != null ) {
				values.add( table.makeColumn( columns[i], jdbcTypeCodes[i] ) );
			}
			else {
				if ( formulas == null ) {
					throw new IllegalStateException( "Column name was null and no formula information was supplied" );
				}
				values.add( table.makeFormula( formulas[i], jdbcTypeCodes[i] ) );
			}
		}

		return values;
	}

	public static List<Column> makeValues(
			SessionFactoryImplementor factory,
			// todo : SqlTypeDescriptor[] ?  int[] would be enough for now, but SqlTypeDescriptor is nicer
			SqlTypeDescriptor[] typeDescriptors,
			String[] columns,
			String[] formulas,
			Table table) {
		assert columns != null;
		assert formulas == null || columns.length == formulas.length;
		assert typeDescriptors != null;
		assert typeDescriptors.length == columns.length;

		final List<Column> values = new ArrayList<>();

		for ( int i = 0; i < columns.length; i++ ) {
			if ( columns[i] != null ) {
				values.add( table.makeColumn( columns[i], typeDescriptors[i].getSqlType() ) );
			}
			else {
				if ( formulas == null ) {
					throw new IllegalStateException( "Column name was null and no formula information was supplied" );
				}
				values.add( table.makeFormula( formulas[i], typeDescriptors[i].getSqlType() ) );
			}
		}

		return values;
	}

	public OrmAttribute buildAttribute(
			PersisterCreationContext creationContext,
			OrmNavigableSource source,
			Value value,
			String propertyName,
			Type propertyType,
			List<Column> columns) {
		if ( propertyType instanceof org.hibernate.orm.type.spi.CollectionType ) {
			assert columns == null || columns.isEmpty();

			return buildPluralAttribute(
					creationContext,
					(Collection) value,
					source,
					propertyName
			);
		}
		else {
			return buildSingularAttribute(
					creationContext,
					source,
					value,
					propertyName,
					propertyType,
					columns
			);
		}
	}

	public AbstractOrmAttribute buildSingularAttribute(
			PersisterCreationContext creationContext,
			OrmNavigableSource source,
			Value value,
			String attributeName,
			Type attributeType,
			List<Column> columns) {
		final SingularAttributeClassification classification = interpretSingularAttributeClassification( attributeType );
		if ( classification == SingularAttributeClassification.ANY ) {
			throw new NotYetImplementedException();
		}
		else if ( classification == SingularAttributeClassification.EMBEDDED ) {
			return new SingularAttributeEmbedded(
					(ManagedTypeImplementor) source,
					attributeName,
					PropertyAccess.DUMMY,
					SingularAttribute.Disposition.NORMAL,
					buildEmbeddablePersister(
							creationContext,
							(EmbeddableContainer) source,
							attributeName,
							(Component) value,
							columns
					)
			);
		}
		else if ( classification == SingularAttributeClassification.BASIC ) {
			// todo : need to be able to locate the AttributeConverter (if one) associated with this singular basic attribute
//			final AttributeConverter attributeConverter = ( (SimpleValue) value ).getAttributeConverterDescriptor().getAttributeConverter();
			final AttributeConverter attributeConverter = null;
			return new SingularAttributeBasic(
					(ManagedTypeImplementor) source,
					attributeName,
					PropertyAccess.DUMMY,
					(BasicType) attributeType,
					SingularAttribute.Disposition.NORMAL,
					attributeConverter,
					columns
			);
		}
		else {
			final EntityType ormEntityType = (EntityType) attributeType;
			return new SingularAttributeEntity(
					(ManagedTypeImplementor) source,
					attributeName,
					PropertyAccess.DUMMY,
					SingularAttribute.Disposition.NORMAL,
					classification,
					ormEntityType,
					columns
			);
		}
	}

	public EmbeddableMapper buildEmbeddablePersister(
			PersisterCreationContext creationContext,
			EmbeddableContainer compositeContainer,
			String localName,
			Component component,
			List<Column> columns) {
		EmbeddableMapper mapper = creationContext.getTypeConfiguration()
				.findEmbeddableMapper( compositeContainer.getRolePrefix() + '.' + localName );
		if ( mapper == null ) {
			mapper = creationContext.getPersisterFactory().createEmbeddablePersister( component, compositeContainer, localName, creationContext );
		}

		return mapper;
	}


	public OrmAttribute buildPluralAttribute(
			PersisterCreationContext creationContext,
			Collection collectionBinding,
			OrmNavigableSource source,
			String propertyName) {
		// todo : resolve cache access
		final CollectionRegionAccessStrategy cachingAccess = null;

		// need PersisterCreationContext - we should always have access to that when building persisters, through finalized initialization
		final CollectionPersister collectionPersister = creationContext.getPersisterFactory().createCollectionPersister(
				collectionBinding,
				(ManagedTypeImplementor) source,
				propertyName,
				cachingAccess,
				creationContext
		);
		creationContext.registerCollectionPersister( collectionPersister );
		return collectionPersister;
	}

	public static org.hibernate.loader.PropertyPath convert(SqmPropertyPath propertyPath) {
		if ( propertyPath.getParent() == null ) {
			return new org.hibernate.loader.PropertyPath( null, propertyPath.getLocalPath() );
		}
		org.hibernate.loader.PropertyPath parent = convert( propertyPath.getParent() );
		return parent.append( propertyPath.getLocalPath() );
	}

	public static CollectionClassification interpretCollectionClassification(Collection collection) {
		return interpretCollectionClassification( collection.getCollectionType() );
	}

	public static interface CollectionMetadata {
		CollectionClassification getCollectionClassification();
		ElementClassification getElementClassification();

		Type getForeignKeyType();
		BasicType getCollectionIdType();
		Type getElementType();
		Type getIndexType();
	}

	public static class CollectionMetadataImpl implements CollectionMetadata {
		private final CollectionClassification collectionClassification;
		private final ElementClassification elementClassification;
		private final Type foreignKeyType;
		private final BasicType collectionIdType;
		private final Type elementType;
		private final Type indexType;

		public CollectionMetadataImpl(
				CollectionClassification collectionClassification,
				ElementClassification elementClassification,
				Type foreignKeyType,
				BasicType collectionIdType,
				Type elementType,
				Type indexType) {
			this.collectionClassification = collectionClassification;
			this.elementClassification = elementClassification;
			this.foreignKeyType = foreignKeyType;
			this.collectionIdType = collectionIdType;
			this.elementType = elementType;
			this.indexType = indexType;
		}

		@Override
		public CollectionClassification getCollectionClassification() {
			return collectionClassification;
		}

		@Override
		public ElementClassification getElementClassification() {
			return elementClassification;
		}

		@Override
		public Type getForeignKeyType() {
			return foreignKeyType;
		}

		@Override
		public BasicType getCollectionIdType() {
			return collectionIdType;
		}

		@Override
		public Type getElementType() {
			return elementType;
		}

		@Override
		public Type getIndexType() {
			return indexType;
		}
	}

	public static CollectionMetadata interpretCollectionMetadata(TypeConfiguration typeConfiguration, CollectionType collectionType) {
		throw new NotYetImplementedException();
//		typeConfiguration.findCollectionPersister(  )
//		final CollectionPersister collectionPersister = factory.().collectionPersister( collectionType.getRole() );
//
//		return new CollectionMetadataImpl(
//				interpretCollectionClassification( collectionType ),
//				interpretElementClassification( collectionPersister ),
//				collectionPersister.getKeyType(),
//				(BasicType) collectionPersister.getIdentifierType(),
//				collectionPersister.getElementType(),
//				collectionPersister.getIndexType()
//		);
	}

	public static CollectionClassification interpretCollectionClassification(CollectionType collectionType) {
		if ( collectionType instanceof BagType
				|| collectionType instanceof IdentifierBagType ) {
			return CollectionClassification.BAG;
		}
		else if ( collectionType instanceof ListType
				|| collectionType instanceof ArrayType ) {
			return CollectionClassification.LIST;
		}
		else if ( collectionType instanceof SetType
				|| collectionType instanceof OrderedSetType
				|| collectionType instanceof SortedSetType ) {
			return CollectionClassification.SET;
		}
		else if ( collectionType instanceof MapType
				|| collectionType instanceof OrderedMapType
				|| collectionType instanceof SortedMapType ) {
			return CollectionClassification.MAP;
		}
		else {
			final Class javaType = collectionType.getReturnedClass();
			if ( Set.class.isAssignableFrom( javaType ) ) {
				return CollectionClassification.SET;
			}
			else if ( Map.class.isAssignableFrom( javaType ) ) {
				return CollectionClassification.MAP;
			}
			else if ( List.class.isAssignableFrom( javaType ) ) {
				return CollectionClassification.LIST;
			}

			return CollectionClassification.BAG;
		}
	}

	private static ElementClassification interpretElementClassification(CollectionPersister collectionPersister) {
		switch ( collectionPersister.getElementDescriptor().getClassification() ) {
			case ANY: {
				return ElementClassification.ANY;
			}
			case EMBEDDABLE: {
				return ElementClassification.EMBEDDABLE;
			}
			case ONE_TO_MANY: {
				return ElementClassification.ONE_TO_MANY;
			}
			case MANY_TO_MANY: {
				return ElementClassification.MANY_TO_MANY;
			}
			default: {
				return ElementClassification.BASIC;
			}
		}
	}

	public static SingularAttributeClassification interpretSingularAttributeClassification(Type attributeType) {
		if ( attributeType instanceof EntityType ) {
			// assume many-to-one for now...
			return SingularAttributeClassification.MANY_TO_ONE;
		}

		if ( attributeType instanceof EmbeddedType ) {
			return SingularAttributeClassification.EMBEDDED;
		}

		if ( attributeType instanceof AnyType ) {
			return SingularAttributeClassification.ANY;
		}

		return SingularAttributeClassification.BASIC;
	}

	public static SingularAttributeClassification interpretIdentifierClassification(Type ormIdType) {
		if ( ormIdType instanceof EmbeddedType ) {
			return SingularAttributeClassification.EMBEDDED;
		}
		else {
			return SingularAttributeClassification.BASIC;
		}
	}
}
