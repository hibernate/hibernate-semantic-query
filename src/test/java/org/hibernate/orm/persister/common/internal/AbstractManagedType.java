/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.common.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.common.spi.OrmAttribute;
import org.hibernate.orm.persister.common.spi.OrmCollectionAttribute;
import org.hibernate.orm.persister.common.spi.OrmListAttribute;
import org.hibernate.orm.persister.common.spi.OrmMapAttribute;
import org.hibernate.orm.persister.common.spi.OrmNavigable;
import org.hibernate.orm.persister.common.spi.OrmPluralAttribute;
import org.hibernate.orm.persister.common.spi.OrmSetAttribute;
import org.hibernate.orm.persister.common.spi.OrmSingularAttribute;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.orm.type.spi.TypeConfigurationAware;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractManagedType<T> implements ManagedTypeImplementor<T>, TypeConfigurationAware {
	private static final Logger log = Logger.getLogger( AbstractManagedType.class );

	private final JavaTypeDescriptor<T> javaTypeDescriptor;
	private final MutabilityPlan mutabilityPlan;
	private final Comparator comparator;

	private ManagedTypeImplementor  superTypeDescriptor;

	private Map<String,OrmAttribute> declaredAttributesByName;

	private TypeConfiguration typeConfiguration;

	public AbstractManagedType(JavaTypeDescriptor<T> javaTypeDescriptor) {
		this(
				javaTypeDescriptor,
				javaTypeDescriptor.getMutabilityPlan(),
				javaTypeDescriptor.getComparator()
		);
	}

	public AbstractManagedType(
			JavaTypeDescriptor<T> javaTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		this.javaTypeDescriptor = javaTypeDescriptor;
		this.mutabilityPlan = mutabilityPlan;
		this.comparator = comparator;
	}

	protected void injectSuperTypeDescriptor(ManagedTypeImplementor superTypeDescriptor) {
		log.debugf(
				"Injecting super-type descriptor [%s] for ManagedTypeImplementor [%s]; was [%s]",
				superTypeDescriptor,
				this,
				this.superTypeDescriptor
		);
		this.superTypeDescriptor = superTypeDescriptor;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	@Override
	public void setTypeConfiguration(TypeConfiguration typeConfiguration) {
		this.typeConfiguration = typeConfiguration;

	}

	@Override
	public ManagedTypeImplementor getSuperType() {
		return superTypeDescriptor;
	}

	public JavaTypeDescriptor<T> getJavaTypeDescriptor() {
		return javaTypeDescriptor;
	}

	@Override
	public boolean canCompositeContainCollections() {
		return true;
	}


	protected void addAttribute(OrmAttribute attribute) {
		if ( declaredAttributesByName == null ) {
			declaredAttributesByName = new HashMap<>();
		}
		declaredAttributesByName.put( attribute.getAttributeName(), attribute );
	}

	@Override
	public OrmAttribute findAttribute(String name) {
		final OrmAttribute declaredAttribute = findDeclaredAttribute( name );
		if ( declaredAttribute != null ) {
			return declaredAttribute;
		}

		if ( getSuperType() != null ) {
			final OrmAttribute superAttribute = getSuperType().findAttribute( name );
			if ( superAttribute != null ) {
				return superAttribute;
			}
		}

		return null;
	}

	@Override
	public OrmAttribute findDeclaredAttribute(String name) {
		if ( declaredAttributesByName == null ) {
			return null;
		}

		return declaredAttributesByName.get( name );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// get groups of attributes

	public void collectDeclaredAttributes(Consumer<Attribute> collector) {
		collectDeclaredAttributes( collector, null );
	}

	@Override
	@SuppressWarnings("unchecked")
	public void collectDeclaredAttributes(Consumer collector, Class restrictionType) {
		if ( declaredAttributesByName != null ) {
			Stream stream = declaredAttributesByName.values().stream();
			if ( restrictionType != null ) {
				stream = stream.filter( restrictionType::isInstance );
			}
			stream.forEach( ormAttribute -> collector.accept( ormAttribute ) );
		}
	}

	private void collectAttributes(Consumer<Attribute> collector) {
		collectAttributes( collector, null );
	}

	@Override
	public void collectAttributes(Consumer collector, Class restrictionType) {
		collectDeclaredAttributes( collector, restrictionType );

		if ( getSuperType() != null  ) {
			getSuperType().collectAttributes( collector, restrictionType );
		}
	}

	@Override
	public Set<Attribute<? super T, ?>> getAttributes() {
		final HashSet<Attribute<? super T, ?>> attributes = new HashSet<>();
		collectAttributes( attributes::add );
		return attributes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Attribute<T, ?>> getDeclaredAttributes() {
		final HashSet<Attribute<T, ?>> attributes = new HashSet<>();
		collectDeclaredAttributes( attributes::add );
		return attributes.stream().collect( Collectors.toSet() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<SingularAttribute<? super T, ?>> getSingularAttributes() {
		final HashSet attributes = new HashSet();
		collectAttributes( attributes::add, SingularAttribute.class );
		return attributes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<SingularAttribute<T,?>> getDeclaredSingularAttributes() {
		final HashSet attributes = new HashSet<>();
		collectDeclaredAttributes( attributes::add, SingularAttribute.class );
		return attributes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<PluralAttribute<? super T, ?, ?>> getPluralAttributes() {
		final HashSet attributes = new HashSet<>();
		collectAttributes( attributes::add, PluralAttribute.class );
		return attributes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<PluralAttribute<T, ?, ?>> getDeclaredPluralAttributes() {
		final HashSet attributes = new HashSet<>();
		collectDeclaredAttributes( attributes::add, PluralAttribute.class );
		return attributes;
	}

	@Override
	public Map<String, OrmAttribute> getAttributesByName() {
		final Map<String, OrmAttribute> attributeMap = new HashMap<>();
		collectAttributes( attributeMap );
		return attributeMap;
	}

	protected void collectAttributes(Map<String, OrmAttribute> attributeMap) {
		attributeMap.putAll( getDeclaredAttributesByName() );
		if ( superTypeDescriptor != null && superTypeDescriptor instanceof AbstractManagedType ) {
			( (AbstractManagedType) superTypeDescriptor ).collectAttributes( attributeMap );
		}
	}

	@Override
	public Map<String, OrmAttribute> getDeclaredAttributesByName() {
		return declaredAttributesByName == null ? Collections.emptyMap() : declaredAttributesByName;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Access to information about a single Attribute

	@Override
	public OrmNavigable findNavigable(String navigableName) {
		OrmNavigable attribute = findDeclaredAttribute( navigableName );
		if ( attribute == null && getSuperType() != null ) {
			attribute = getSuperType().findNavigable( navigableName );
		}
		return attribute;
	}

	@Override
	public OrmAttribute getAttribute(String name) {
		return getAttribute( name, null );
	}

	protected OrmAttribute getAttribute(String name, Class resultType) {
		OrmAttribute attribute = findDeclaredAttribute( name, resultType );
		if ( attribute == null && getSuperType() != null ) {
			attribute = getSuperType().findDeclaredAttribute( name, resultType );
		}

		if ( attribute == null ) {
			throw new IllegalArgumentException( "Could not resolve attribute named [" + name + "] relative to [" + this.asLoggableText() + "]" );
		}

		return attribute;
	}

	@Override
	public OrmAttribute findDeclaredAttribute(String name, Class resultType) {
		final OrmAttribute ormAttribute = declaredAttributesByName.get( name );
		if ( ormAttribute == null ) {
			return null;
		}

		if ( ormAttribute instanceof OrmSingularAttribute ) {
			checkAttributeType( (OrmSingularAttribute) ormAttribute, resultType );
		}
		else {
			checkAttributeType( (OrmPluralAttribute) ormAttribute, resultType );
		}

		return ormAttribute;
	}

	protected void checkAttributeType(OrmSingularAttribute ormAttribute, Class resultType) {
		checkType(  ormAttribute.getName(), ormAttribute.getJavaType(), resultType );
	}

	protected void checkAttributeType(OrmPluralAttribute ormAttribute, Class resultType) {
		checkType(  ormAttribute.getName(), ormAttribute.getElementType().getJavaType(), resultType );
	}

	@SuppressWarnings("unchecked")
	protected void checkType(String name, Class attributeType, Class resultType) {
		if ( resultType != null && attributeType != null ) {
			if ( !resultType.isAssignableFrom( attributeType ) ) {
				throw new IllegalArgumentException(
						"Found attribute for given name [" + name +
								"], but its type [" + attributeType +
								"] is not assignable to the requested type [" + resultType + "]"
				);
			}
		}
	}

	@Override
	public OrmAttribute getDeclaredAttribute(String name) {
		return getDeclaredAttribute( name, null );
	}

	public OrmAttribute getDeclaredAttribute(String name, Class javaType) {
		final OrmAttribute attribute = findDeclaredAttribute( name, javaType );
		if ( attribute == null ) {
			throw new IllegalArgumentException( "Could not resolve attribute named [" + name + "] relative to [" + this.asLoggableText() + "]" );
		}
		return attribute;
	}

	@Override
	public OrmSingularAttribute getSingularAttribute(String name) {
		return getSingularAttribute( name, null );
	}

	@Override
	public OrmSingularAttribute getSingularAttribute(String name, Class type) {
		return (OrmSingularAttribute) getAttribute( name, type );
	}

	@Override
	public SingularAttribute getDeclaredSingularAttribute(String name) {
		return getDeclaredSingularAttribute( name, null );
	}

	@Override
	public OrmSingularAttribute getDeclaredSingularAttribute(String name, Class type) {
		return (OrmSingularAttribute) getDeclaredAttribute( name, type );
	}

	@Override
	public OrmCollectionAttribute getCollection(String name) {
		return getCollection( name, null );
	}

	@Override
	public OrmCollectionAttribute getCollection(String name, Class elementType) {
		return (OrmCollectionAttribute) getAttribute( name, elementType );
	}

	@Override
	public OrmCollectionAttribute getDeclaredCollection(String name) {
		return getDeclaredCollection( name, null );
	}

	@Override
	public OrmCollectionAttribute getDeclaredCollection(String name, Class elementType) {
		return (OrmCollectionAttribute) getDeclaredAttribute( name, elementType );
	}

	@Override
	public OrmListAttribute getList(String name) {
		return getList( name, null );
	}

	@Override
	public OrmListAttribute getList(String name, Class elementType) {
		return (OrmListAttribute) getAttribute( name, elementType );
	}

	@Override
	public OrmListAttribute getDeclaredList(String name) {
		return getDeclaredList( name, null );
	}

	@Override
	public OrmListAttribute getDeclaredList(String name, Class elementType) {
		return (OrmListAttribute) getDeclaredAttribute( name, elementType );
	}

	@Override
	public OrmMapAttribute getMap(String name) {
		return getMap( name, null, null );
	}

	@Override
	public OrmMapAttribute getMap(String name, Class keyType, Class valueType) {
		final OrmMapAttribute mapAttribute = (OrmMapAttribute) getAttribute( name, valueType );
		if ( mapAttribute == null ) {
			return null;
		}

		checkMapKeyType( name, mapAttribute.getKeyJavaType(), keyType  );

		return mapAttribute;
	}

	@SuppressWarnings("unchecked")
	private void checkMapKeyType(String name, Class attributeType, Class resultType) {
		checkType( name + ".key", attributeType, resultType );
	}

	@Override
	public OrmMapAttribute getDeclaredMap(String name) {
		return getDeclaredMap( name, null, null );
	}

	@Override
	public OrmMapAttribute getDeclaredMap(String name, Class keyType, Class valueType) {
		final OrmMapAttribute mapAttribute = (OrmMapAttribute) getDeclaredAttribute( name, valueType );
		if ( mapAttribute == null ) {
			return null;
		}

		checkMapKeyType( name, keyType, mapAttribute.getKeyJavaType() );

		return mapAttribute;
	}

	@Override
	public OrmSetAttribute getSet(String name) {
		return getSet( name, null );
	}

	@Override
	public OrmSetAttribute getSet(String name, Class elementType) {
		return (OrmSetAttribute) getAttribute( name, elementType );
	}

	@Override
	public OrmSetAttribute getDeclaredSet(String name) {
		return getDeclaredSet( name, null );
	}

	@Override
	public OrmSetAttribute getDeclaredSet(String name, Class elementType) {
		return (OrmSetAttribute) getDeclaredAttribute( name, elementType );
	}
}
