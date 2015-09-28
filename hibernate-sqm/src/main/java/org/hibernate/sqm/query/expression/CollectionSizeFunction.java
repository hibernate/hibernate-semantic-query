/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.from.FromElement;

/**
 * Represents the {@code SIZE()} function.
 *
 * @author Steve Ebersole
 * @author Gunnar Morling
 */
public class CollectionSizeFunction implements Expression {

	private final String fromElementAlias;
	private final AttributeDescriptor attributeDescriptor;

	public CollectionSizeFunction(FromElement fromElement, AttributeDescriptor attributeDescriptor) {
		this.fromElementAlias = fromElement.getAlias();
		this.attributeDescriptor = attributeDescriptor;
	}

	public String getFromElementAlias() {
		return fromElementAlias;
	}

	public AttributeDescriptor getAttributeDescriptor() {
		return attributeDescriptor;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return StandardBasicTypeDescriptors.INSTANCE.LONG;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCollectionSizeFunction( this );
	}
}
