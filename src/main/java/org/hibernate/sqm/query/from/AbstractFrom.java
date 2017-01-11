/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;

import org.jboss.logging.Logger;

/**
 * Convenience base class for FromElement implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractFrom implements SqmFrom {
	private static final Logger log = Logger.getLogger( AbstractFrom.class );

	private final FromElementSpace fromElementSpace;
	private final String uid;
	private final String alias;
	private final SqmNavigableBinding binding;
	private final SqmExpressableTypeEntity subclassIndicator;

	protected AbstractFrom(
			FromElementSpace fromElementSpace,
			String uid,
			String alias,
			SqmNavigableBinding binding,
			SqmExpressableTypeEntity subclassIndicator) {
		this.fromElementSpace = fromElementSpace;
		this.uid = uid;
		this.alias = alias;
		this.binding = binding;
		this.subclassIndicator = subclassIndicator;
	}

	@Override
	public SqmNavigableBinding getBinding() {
		return binding;
	}

//	@Override
//	public SqmNavigableSource getBoundNavigable() {
//		return binding.getBoundNavigable();
//	}

	@Override
	public FromElementSpace getContainingSpace() {
		return fromElementSpace;
	}

	@Override
	public String getUniqueIdentifier() {
		return uid;
	}

	@Override
	public String getIdentificationVariable() {
		return alias;
	}

	@Override
	public SqmExpressableTypeEntity getIntrinsicSubclassIndicator() {
		return subclassIndicator;
	}
}
