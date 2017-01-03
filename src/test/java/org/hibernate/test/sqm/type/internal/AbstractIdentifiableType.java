/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.test.sqm.type.spi.IdentifiableType;
import org.hibernate.test.sqm.type.spi.IdentifierDescriptor;
import org.hibernate.test.sqm.type.spi.IdentifierDescriptorSingleAttribute;
import org.hibernate.test.sqm.type.spi.SingularAttributeBasic;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractIdentifiableType extends AbstractManagedType implements IdentifiableType {
	private final IdentifiableType superType;

	private IdentifierDescriptor identifierDescriptor;
	private SingularAttributeBasic versionAttribute;

	public AbstractIdentifiableType(
			String typeName,
			IdentifiableType superType) {
		super( typeName );
		this.superType = superType;
	}

	public AbstractIdentifiableType(
			Class javaType,
			IdentifiableType superType) {
		super( javaType );
		this.superType = superType;
	}

	@Override
	public IdentifiableType getSuperType() {
		return superType;
	}

	@Override
	public IdentifierDescriptor getIdentifierDescriptor() {
		return identifierDescriptor;
	}

	public void setIdentifierDescriptor(IdentifierDescriptor identifierDescriptor) {
		this.identifierDescriptor = identifierDescriptor;
	}

	@Override
	public SingularAttributeBasic getVersionAttribute() {
		return versionAttribute;
	}

	public void setVersionAttribute(SingularAttributeBasic versionAttribute) {
		this.versionAttribute = versionAttribute;
	}

	// todo : this does not really handle declared attributes versus non-declared.  Need to come back and add that

	@Override
	public SingularAttribute getId(Class type) {
		if ( identifierDescriptor instanceof IdentifierDescriptorSingleAttribute ) {
			return ( (IdentifierDescriptorSingleAttribute) identifierDescriptor ).getIdAttribute();
		}
		throw new IllegalArgumentException(
				"Identifier for IdentifiableType [" + getTypeName() + "] is not a singular attribute"
		);
	}

	@Override
	public SingularAttribute getDeclaredId(Class type) {
		return getId( type );
	}

	@Override
	public SingularAttribute getVersion(Class type) {
		return getVersionAttribute();
	}

	@Override
	public SingularAttribute getDeclaredVersion(Class type) {
		return getVersionAttribute();
	}

	@Override
	public javax.persistence.metamodel.IdentifiableType getSupertype() {
		return superType;
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return identifierDescriptor instanceof IdentifierDescriptorSingleAttribute;
	}

	@Override
	public boolean hasVersionAttribute() {
		return versionAttribute != null;
	}

	@Override
	public Set<SingularAttribute> getIdClassAttributes() {
		if ( identifierDescriptor instanceof IdentifierDescriptorNonAggregatedEmbeddedImpl ) {
			return ( ( IdentifierDescriptorNonAggregatedEmbeddedImpl) identifierDescriptor ).getIdentifierAttributes()
					.stream()
					.collect( Collectors.toSet() );
		}
		throw new IllegalArgumentException(
				"Identifier for IdentifiableType [" + getTypeName() + "] is not a non-aggreated composite"
		);
	}

	@Override
	public Type<?> getIdType() {
		return identifierDescriptor.getIdType();
	}
}
