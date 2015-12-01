/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;

/**
 * @author Steve Ebersole
 */
public class TreatedFromElement implements FromElement {
	private final FromElement wrapped;
	private final EntityType treatedAs;

	public TreatedFromElement(FromElement wrapped, EntityType treatedAs) {
		this.wrapped = wrapped;
		this.treatedAs = treatedAs;
	}

	public FromElement getWrapped() {
		return wrapped;
	}

	@Override
	public FromElementSpace getContainingSpace() {
		return wrapped.getContainingSpace();
	}

	@Override
	public String getAlias() {
		return wrapped.getAlias();
	}

	@Override
	public Bindable getBindableModelDescriptor() {
		return treatedAs;
	}

	@Override
	public Attribute resolveAttribute(String attributeName) {
		return wrapped.resolveAttribute( attributeName );
	}

	@Override
	public void addTreatedAs(EntityType typeDescriptor) {
		wrapped.addTreatedAs( typeDescriptor );
	}

	public Bindable getBaseBindableModelDescriptor() {
		return wrapped.getBindableModelDescriptor();
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		return getWrapped();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new UnsupportedOperationException( "see todo.md comment" );
	}
}
