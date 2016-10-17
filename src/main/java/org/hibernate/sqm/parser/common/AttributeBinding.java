/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * @author Steve Ebersole
 */
public class AttributeBinding implements DomainReferenceBinding {
	private final DomainReferenceBinding lhs;
	private final AttributeReference attribute;
	private SqmAttributeJoin join;

	public AttributeBinding(
			DomainReferenceBinding lhs,
			AttributeReference attribute) {
		this.lhs = lhs;
		this.attribute = attribute;
	}

	public AttributeBinding(
			DomainReferenceBinding lhs,
			AttributeReference attribute,
			SqmAttributeJoin join) {
		this( lhs, attribute );
		this.join = join;
	}

	public void injectAttributeJoin(SqmAttributeJoin attributeJoin) {
		if ( this.join != null && this.join != attributeJoin ) {
			throw new IllegalArgumentException( "Attempting to create multiple SqmAttributeJoin references for a single AttributeBinding" );
		}
		this.join = attributeJoin;
	}

	public DomainReferenceBinding getLhs() {
		return lhs;
	}

	public AttributeReference getAttribute() {
		return attribute;
	}

	@Override
	public SqmAttributeJoin getFromElement() {
		return join;
	}

	@Override
	public AttributeReference getBoundDomainReference() {
		return attribute;
	}

	@Override
	public DomainReference getExpressionType() {
		return getBoundDomainReference();
	}

	@Override
	public DomainReference getInferableType() {
		return getBoundDomainReference();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAttributeReferenceExpression( this );
	}

	@Override
	public String asLoggableText() {
		if ( join == null || join.getIdentificationVariable() == null ) {
			return "AttributeBinding(" + lhs.asLoggableText() + '.' + attribute.getAttributeName() + ")";
		}
		else {
			return "AttributeBinding(" + lhs.asLoggableText() + '.' + attribute.getAttributeName() + " : " + join.getIdentificationVariable() + ")";
		}
	}
}
