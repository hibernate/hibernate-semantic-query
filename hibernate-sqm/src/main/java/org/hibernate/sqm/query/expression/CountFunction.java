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
public class CountFunction extends AbstractAggregateFunction {
	public CountFunction(Expression argument, boolean distinct) {
		super( argument, distinct );
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return StandardBasicTypeDescriptors.INSTANCE.LONG;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCountFunction( this );
	}
}
