/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmNavigable;

/**
 * @author Steve Ebersole
 */
public class SqmMaxElementBindingBasic
		extends AbstractSpecificSqmElementBinding
		implements SqmRestrictedCollectionElementBindingBasic, SqmMaxElementBinding {
	public SqmMaxElementBindingBasic(SqmPluralAttributeBinding pluralAttributeBinding) {
		super( pluralAttributeBinding );
	}

	@Override
	public SqmNavigable getExpressionType() {
		return getBoundNavigable();
	}

	@Override
	public SqmNavigable getInferableType() {
		return getBoundNavigable();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMaxElementBinding( this );
	}

	@Override
	public String asLoggableText() {
		return "MAXELEMENT( " + getPluralAttributeBinding().asLoggableText() + ")";
	}

	@Override
	public SqmPluralAttributeBinding getSourceBinding() {
		return  getPluralAttributeBinding();
	}

	@Override
	public SqmNavigable getBoundNavigable() {
		return getPluralAttributeBinding().getBoundNavigable().getElementReference();
	}
}
