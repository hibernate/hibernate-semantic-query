/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import java.lang.reflect.Member;

import org.hibernate.test.sqm.type.spi.EntityType;
import org.hibernate.test.sqm.type.spi.ManagedType;
import org.hibernate.test.sqm.type.spi.SingularAttributeEntity;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEntityImpl extends AbstractSingularAttribute implements SingularAttributeEntity {
	private final Disposition disposition;
	private final Member javaMember;
	private final boolean isManyToOne;

	public SingularAttributeEntityImpl(
			ManagedType declaringType,
			String name,
			EntityType type,
			Disposition disposition,
			Member javaMember,
			boolean isManyToOne) {
		super( declaringType, name, type );
		this.disposition = disposition;
		this.javaMember = javaMember;
		this.isManyToOne = isManyToOne;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return isManyToOne ? PersistentAttributeType.MANY_TO_ONE : PersistentAttributeType.ONE_TO_ONE;
	}

	@Override
	public EntityType getType() {
		return (EntityType) super.getType();
	}

	@Override
	public Class getJavaType() {
		return getType().getJavaType();
	}

	@Override
	public Member getJavaMember() {
		return javaMember;
	}

	@Override
	public boolean isAssociation() {
		return true;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.SINGULAR_ATTRIBUTE;
	}

	@Override
	public Class getBindableJavaType() {
		return getJavaType();
	}

	@Override
	public Disposition getDisposition() {
		return disposition;
	}
}
