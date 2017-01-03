/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

/**
 * @author Steve Ebersole
 */
public class EmbeddableTypeImpl extends AbstractManagedType implements EmbeddableType {
	public EmbeddableTypeImpl(String typeName) {
		super( typeName );
	}

	public EmbeddableTypeImpl(Class javaType) {
		super( javaType );
	}

	@Override
	public ManagedType getSuperType() {
		// for now...
		return null;
	}

	@Override
	public Type getBoundType() {
		return this;
	}

	@Override
	public ManagedType asManagedType() {
		return this;
	}

	@Override
	public String asLoggableText() {
		return "Embeddable(" + getTypeName() + ")";
	}
}
