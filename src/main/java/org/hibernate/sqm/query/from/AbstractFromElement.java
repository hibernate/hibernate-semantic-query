/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.Type;

/**
 * Convenience base class for FromElement implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractFromElement implements FromElement {
	private final FromElementSpace fromElementSpace;
	private final String alias;
	private final Bindable bindableModelDescriptor;

	private Set<Type> treatedAsTypeDescriptors;

	protected AbstractFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			Bindable bindableModelDescriptor) {
		this.fromElementSpace = fromElementSpace;
		this.alias = alias;
		this.bindableModelDescriptor = bindableModelDescriptor;
	}

	@Override
	public FromElementSpace getContainingSpace() {
		return fromElementSpace;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public Bindable getBindableModelDescriptor() {
		return bindableModelDescriptor;
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		return this;
	}

	// todo : we likely need to capture information about where the TREAT AS was defined as well
	// 		since that often dictates how the TREAT AS manifests into SQL

	@Override
	public void addTreatedAs(EntityType typeDescriptor) {
		if ( treatedAsTypeDescriptors == null ) {
			treatedAsTypeDescriptors = new HashSet<Type>();
		}
		treatedAsTypeDescriptors.add( typeDescriptor );
	}

	public Set<Type> getTreatedAsTypeDescriptors() {
		if ( treatedAsTypeDescriptors == null ) {
			return Collections.emptySet();
		}
		else {
			return Collections.unmodifiableSet( treatedAsTypeDescriptors );
		}
	}
}
