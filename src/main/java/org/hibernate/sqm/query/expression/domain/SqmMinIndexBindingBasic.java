/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public class SqmMinIndexBindingBasic extends AbstractSpecificSqmCollectionIndexBinding implements SqmMinIndexBinding {
	public SqmMinIndexBindingBasic(SqmPluralAttributeBinding attributeBinding) {
		super( attributeBinding );
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return getPluralAttributeBinding().getBoundNavigable().getIndexReference();
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMinIndexFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "MININDEX(" + getPluralAttributeBinding().asLoggableText() + ")";
	}
}
