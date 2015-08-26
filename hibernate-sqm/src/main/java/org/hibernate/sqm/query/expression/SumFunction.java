/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class SumFunction extends AbstractAggregateFunction implements AggregateFunction {
	private final TypeDescriptor typeDescriptor;

	public SumFunction(Expression argument, boolean distinct) {
		super( argument, distinct );

		if ( argument.getTypeDescriptor() == StandardBasicTypeDescriptors.INSTANCE.BIG_INTEGER ) {
			typeDescriptor = StandardBasicTypeDescriptors.INSTANCE.BIG_INTEGER;
		}
		else if ( argument.getTypeDescriptor() == StandardBasicTypeDescriptors.INSTANCE.BIG_DECIMAL ) {
			typeDescriptor = StandardBasicTypeDescriptors.INSTANCE.BIG_DECIMAL;
		}
		else if ( argument.getTypeDescriptor() == StandardBasicTypeDescriptors.INSTANCE.LONG
				|| argument.getTypeDescriptor() == StandardBasicTypeDescriptors.INSTANCE.SHORT
				|| argument.getTypeDescriptor() == StandardBasicTypeDescriptors.INSTANCE.INTEGER ) {
			typeDescriptor = StandardBasicTypeDescriptors.INSTANCE.LONG;
		}
		else if ( argument.getTypeDescriptor() == StandardBasicTypeDescriptors.INSTANCE.FLOAT
				|| argument.getTypeDescriptor() == StandardBasicTypeDescriptors.INSTANCE.DOUBLE)  {
			typeDescriptor = StandardBasicTypeDescriptors.INSTANCE.DOUBLE;
		}
		else {
			typeDescriptor = argument.getTypeDescriptor();
		}
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSumFunction( this );
	}
}
