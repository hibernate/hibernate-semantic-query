/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class TreatedFromElement implements FromElement {
	private final FromElement wrapped;
	private final TypeDescriptor treatedAs;

	public TreatedFromElement(FromElement wrapped, TypeDescriptor treatedAs) {
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
	public TypeDescriptor getTypeDescriptor() {
		return treatedAs;
	}

	@Override
	public void addTreatedAs(TypeDescriptor typeDescriptor) {
		wrapped.addTreatedAs( typeDescriptor );
	}

	public TypeDescriptor getBaseTypeDescriptor() {
		return wrapped.getTypeDescriptor();
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
