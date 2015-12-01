/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeImpl implements SingularAttribute {
	private final Classification classification;
	private final ManagedType declaringType;
	private final String name;
	private final Type type;

	private boolean isId;
	private boolean isVersion;

	public SingularAttributeImpl(
			ManagedType declaringType,
			String name,
			Classification classification,
			Type type) {
		this.declaringType = declaringType;
		this.name = name;
		this.classification = classification;
		this.type = type;
	}

	@Override
	public ManagedType getDeclaringType() {
		return declaringType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Type getBoundType() {
		return getType();
	}

	@Override
	public ManagedType asManagedType() {
		// todo : for now, just let the ClassCastException happen
		return (ManagedType) type;
	}

	@Override
	public Classification getAttributeTypeClassification() {
		return classification;
	}

	@Override
	public boolean isId() {
		return isId;
	}

	@Override
	public boolean isVersion() {
		return isVersion;
	}
}
