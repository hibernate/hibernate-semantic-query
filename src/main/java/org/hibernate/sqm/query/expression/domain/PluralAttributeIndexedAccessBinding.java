/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexedAccessBinding implements DomainReferenceBinding {
	private final PluralAttributeBinding attributeBinding;
	private final SqmExpression indexSelectionExpression;

	public PluralAttributeIndexedAccessBinding(
			PluralAttributeBinding pluralAttributeBinding,
			SqmExpression indexSelectionExpression) {
		this.attributeBinding = pluralAttributeBinding;
		this.indexSelectionExpression = indexSelectionExpression;
	}

	public PluralAttributeBinding getPluralAttributeBinding() {
		return attributeBinding;
	}

	public SqmExpression getIndexSelectionExpression() {
		return indexSelectionExpression;
	}

	@Override
	public DomainReference getBoundDomainReference() {
		return attributeBinding.getAttribute().getElementReference();
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
		return attributeBinding.getFromElement();
	}
}
