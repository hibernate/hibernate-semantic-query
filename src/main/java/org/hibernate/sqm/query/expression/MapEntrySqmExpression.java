/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.parser.common.AttributeBinding;

/**
 * Represents the ENTRY() function for obtaining the map entries from a {@code Map}-typed association.
 *
 * @author Gunnar Morling
 * @author Steve Ebersole
 */
public class MapEntrySqmExpression implements SqmExpression {
	private final AttributeBinding attributeBinding;

	public MapEntrySqmExpression(AttributeBinding attributeBinding) {
		this.attributeBinding = attributeBinding;
	}

	public AttributeBinding getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public DomainReference getExpressionType() {
		return null;
	}

	@Override
	public DomainReference getInferableType() {
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMapEntryFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "ENTRY(" + attributeBinding.asLoggableText() + ")";
	}
}
