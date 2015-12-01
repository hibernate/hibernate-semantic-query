/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;

/**
 * @author Steve Ebersole
 */
public class LiteralStringExpression extends AbstractLiteralExpressionImpl<String> {
	public LiteralStringExpression(String value, BasicType<String> typeDescriptor) {
		super( value, typeDescriptor );
	}

	@Override
	protected void validateInferredType(Class javaType) {
		if ( !String.class.equals( javaType ) ) {
			throw new TypeInferenceException( "Inferred type [" + javaType + "] was not convertible to String" );
		}
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralStringExpression( this );
	}
}
