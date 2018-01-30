/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAttributeReference<A extends AttributeDescriptor>
		implements AttributeReference {
	private final SqmNavigableReference lhs;
	private final A attribute;
	private SqmAttributeJoin join;

	private final PropertyPath propertyPath;

	public AbstractAttributeReference(SqmNavigableReference lhs, A attribute) {
		if ( lhs == null ) {
			throw new IllegalArgumentException( "Source for AttributeBinding cannot be null" );
		}
		if ( attribute == null ) {
			throw new IllegalArgumentException( "Attribute for AttributeBinding cannot be null" );
		}

		this.lhs = lhs;
		this.attribute = attribute;

		this.propertyPath = lhs.getFromElement().getPropertyPath().append( attribute.getAttributeName() );
	}

	public AbstractAttributeReference(
			SqmNavigableReference lhs,
			A attribute,
			SqmAttributeJoin join) {
		this( lhs, attribute );
		injectAttributeJoin( join );
	}

	@Override
	public void injectAttributeJoin(SqmAttributeJoin attributeJoin) {
		if ( this.join != null && this.join != attributeJoin ) {
			throw new IllegalArgumentException( "Attempting to create multiple SqmAttributeJoin references for a single AttributeBinding" );
		}
		this.join = attributeJoin;
	}

	@Override
	public SqmNavigableReference getLhs() {
		return lhs;
	}

	@Override
	public A getAttribute() {
		return attribute;
	}

	@Override
	public SqmAttributeJoin getFromElement() {
		return join;
	}

	@Override
	public A getBoundDomainReference() {
		return attribute;
	}

	@Override
	public A getExpressionType() {
		return getBoundDomainReference();
	}

	@Override
	public A getInferableType() {
		return getBoundDomainReference();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAttributeReferenceExpression( this );
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public String asLoggableText() {
		if ( join == null || join.getIdentificationVariable() == null ) {
			return getClass().getSimpleName() + '(' + lhs.asLoggableText() + '.' + attribute.getAttributeName() + ")";
		}
		else {
			return getClass().getSimpleName() + '(' + lhs.asLoggableText() + '.' + attribute.getAttributeName() + " : " + join.getIdentificationVariable() + ")";
		}
	}
}
