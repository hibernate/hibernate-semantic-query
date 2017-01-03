/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.hibernate.test.sqm.type.spi.BasicType;
import org.hibernate.test.sqm.type.spi.ManagedType;
import org.hibernate.test.sqm.type.spi.SingularAttributeBasic;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeBasicImpl extends AbstractSingularAttribute implements SingularAttributeBasic {
	private final Disposition disposition;
	private final Member javaMember;

	private final boolean optional;

	public SingularAttributeBasicImpl(
			ManagedType declaringType,
			String name,
			BasicType type,
			Disposition disposition,
			Member javaMember,
			boolean optional) {
		super( declaringType, name, type );
		this.disposition = disposition;
		this.javaMember = javaMember;
		this.optional = optional;
	}

	@Override
	public BasicType getType() {
		return (BasicType) super.getType();
	}

	@Override
	public Disposition getDisposition() {
		return disposition;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public Class getJavaType() {
		if ( javaMember instanceof Method ) {
			return ( (Method) javaMember ).getReturnType();
		}
		else {
			return ( (Field) javaMember ).getType();
		}
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
