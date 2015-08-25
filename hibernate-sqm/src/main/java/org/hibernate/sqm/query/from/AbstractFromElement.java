/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.from;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * Convenience base class for FromElement implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractFromElement implements FromElement {
	private final FromElementSpace fromElementSpace;
	private final String alias;
	private final TypeDescriptor typeDescriptor;

	private Set<TypeDescriptor> treatedAsTypeDescriptors;

	protected AbstractFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			TypeDescriptor typeDescriptor) {
		this.fromElementSpace = fromElementSpace;
		this.alias = alias;
		this.typeDescriptor = typeDescriptor;
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
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		return this;
	}

	// todo : we likely need to capture information about where the TREAT AS was defined as well
	// 		since that often dictates how the TREAT AS manifests into SQL

	@Override
	public void addTreatedAs(TypeDescriptor typeDescriptor) {
		if ( treatedAsTypeDescriptors == null ) {
			treatedAsTypeDescriptors = new HashSet<TypeDescriptor>();
		}
		treatedAsTypeDescriptors.add( typeDescriptor );
	}

	public Set<TypeDescriptor> getTreatedAsTypeDescriptors() {
		if ( treatedAsTypeDescriptors == null ) {
			return Collections.emptySet();
		}
		else {
			return Collections.unmodifiableSet( treatedAsTypeDescriptors );
		}
	}
}
