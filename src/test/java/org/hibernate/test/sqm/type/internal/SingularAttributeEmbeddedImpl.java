/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import java.lang.reflect.Member;

import org.hibernate.test.sqm.type.spi.EmbeddableType;
import org.hibernate.test.sqm.type.spi.ManagedType;
import org.hibernate.test.sqm.type.spi.SingularAttributeEmbedded;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEmbeddedImpl extends AbstractSingularAttribute implements SingularAttributeEmbedded  {
	private final Disposition disposition;
	private Member javaMember;

	public SingularAttributeEmbeddedImpl(
			ManagedType declaringType,
			String name,
			EmbeddableType type,
			Disposition disposition,
			Member javaMember) {
		super( declaringType, name, type );
		this.disposition = disposition;
		this.javaMember = javaMember;
	}

	@Override
	public EmbeddableType getType() {
		return (EmbeddableType) super.getType();
	}

	@Override
	public Disposition getDisposition() {
		return disposition;
	}

	@Override
	public boolean isOptional() {
		return true;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return PersistentAttributeType.EMBEDDED;
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
		return false;
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
}
