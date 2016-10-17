/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexedAccessBinding implements DomainReferenceBinding {
	private final AttributeBinding pluralAttributeBinding;
	private final SqmExpression indexSelectionExpression;

	public PluralAttributeIndexedAccessBinding(
			AttributeBinding pluralAttributeBinding,
			SqmExpression indexSelectionExpression) {
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.indexSelectionExpression = indexSelectionExpression;
	}

	public AttributeBinding getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	public SqmExpression getIndexSelectionExpression() {
		return indexSelectionExpression;
	}

	@Override
	public DomainReference getBoundDomainReference() {
		return ( (PluralAttributeReference) pluralAttributeBinding.getAttribute() ).getElementReference();
	}

	@Override
	public DomainReference getExpressionType() {
		return getBoundDomainReference();
	}

	@Override
	public DomainReference getInferableType() {
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
		return pluralAttributeBinding.getFromElement();
	}
}
