/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.query.sqm.produce.paths.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.paths.spi.SemanticPathResolutionContext;
import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexedAccessReference implements SqmNavigableReference {
	private final PluralAttributeReference attributeBinding;
	private final SqmExpression indexSelectionExpression;
	private final PropertyPath propertyPath;

	public PluralAttributeIndexedAccessReference(
			PluralAttributeReference pluralAttributeBinding,
			SqmExpression indexSelectionExpression) {
		this.attributeBinding = pluralAttributeBinding;
		this.indexSelectionExpression = indexSelectionExpression;
		this.propertyPath = pluralAttributeBinding.getPropertyPath().append( "{indexes}" );
	}

	public PluralAttributeReference getPluralAttributeBinding() {
		return attributeBinding;
	}

	public SqmExpression getIndexSelectionExpression() {
		return indexSelectionExpression;
	}

	@Override
	public Navigable getBoundDomainReference() {
		return attributeBinding.getAttribute().getElementReference();
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
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new UnsupportedOperationException( "see todo comment" );
	}

	@Override
	public String asLoggableText() {
		return getFromElement().asLoggableText();
	}

	@Override
	public SqmFrom getFromElement() {
		return attributeBinding.getFromElement();
	}

	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		return null;
	}

	@Override
	public SqmNavigableReference resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey, boolean isTerminal, SemanticPathResolutionContext context) {
		return null;
	}
}
