/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.domain.PluralAttributeDescriptor;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * Models an explicit reference to the elements of the collection (generally via the VALUES() function).
 *
 * @author Steve Ebersole
 */
public class PluralAttributeElementReference implements SqmNavigableReference {
	private final PluralAttributeReference attributeBinding;
	private final PluralAttributeDescriptor pluralAttributeReference;
	private final PropertyPath propertyPath;

	public PluralAttributeElementReference(PluralAttributeReference pluralAttributeBinding) {
		this.attributeBinding = pluralAttributeBinding;
		this.pluralAttributeReference = pluralAttributeBinding.getAttribute();
		this.propertyPath = pluralAttributeBinding.getPropertyPath().append( "{elements}" );
	}

	public PluralAttributeReference getPluralAttributeBinding() {
		return attributeBinding;
	}

	public PluralAttributeDescriptor getPluralAttributeReference() {
		return pluralAttributeReference;
	}

	@Override
	public SqmFrom getFromElement() {
		return attributeBinding.getFromElement();
	}

	@Override
	public Navigable getBoundDomainReference() {
		return pluralAttributeReference.getElementReference();
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public Navigable getExpressionType() {
		return getBoundDomainReference();
	}

	@Override
	public Navigable getInferableType() {
		return getBoundDomainReference();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitPluralAttributeElementBinding( this );
	}

	@Override
	public String asLoggableText() {
		return "VALUE(" + attributeBinding.asLoggableText() + ")";
	}

	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new NotYetImplementedException(  );
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		throw new SemanticException( "Cannot index-access cannot be applied to collection elements" );
	}
}
