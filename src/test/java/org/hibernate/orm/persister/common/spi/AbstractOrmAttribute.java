/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.common.spi;

import java.lang.reflect.Member;

import org.hibernate.property.access.spi.PropertyAccess;

/**
 * Base class for Attribute implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractOrmAttribute<O,T> implements OrmAttribute<O,T> {
	private final ManagedTypeImplementor container;
	private final String name;
	private final PropertyAccess access;

	public AbstractOrmAttribute(
			ManagedTypeImplementor container,
			String name,
			PropertyAccess access) {
		this.container = container;
		this.name = name;
		this.access = access;
	}

	@Override
	public ManagedTypeImplementor getSource() {
		return container;
	}

	@Override
	public String getAttributeName() {
		return name;
	}

	@Override
	public String getNavigableName() {
		return getAttributeName();
	}

	@Override
	public Member getJavaMember() {
		return access.getGetter().getMember();
	}
}
