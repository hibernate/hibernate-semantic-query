/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.IdentifiableType;
import org.hibernate.sqm.domain.IdentifierDescriptor;
import org.hibernate.sqm.domain.SingularAttribute;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractIdentifiableType extends AbstractManagedType implements IdentifiableType {
	private final IdentifiableType superType;

	private IdentifierDescriptor identifierDescriptor;
	private SingularAttribute versionAttribute;

	public AbstractIdentifiableType(
			String typeName,
			IdentifiableType superType) {
		super( typeName );
		this.superType = superType;

		// by default define simple IdentifierDescriptor of Integer type named pk
		this.identifierDescriptor = new SingleAttributeIdentifierDescriptor( this, "pk", StandardBasicTypeDescriptors.INSTANCE.INTEGER );
	}

	public AbstractIdentifiableType(
			Class javaType,
			IdentifiableType superType) {
		super( javaType );
		this.superType = superType;

		// by default define simple IdentifierDescriptor of Integer type named pk
		this.identifierDescriptor = new SingleAttributeIdentifierDescriptor( this, "pk", StandardBasicTypeDescriptors.INSTANCE.INTEGER );
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
	public SingularAttribute getVersionAttribute() {
		return versionAttribute;
	}

	public void setVersionAttribute(SingularAttribute versionAttribute) {
		this.versionAttribute = versionAttribute;
	}

}
