/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.navigable.internal;

import org.hibernate.query.sqm.produce.navigable.spi.AbstractNavigableReferenceBuilderContext;
import org.hibernate.query.sqm.produce.spi.SemanticQueryBuilder;
import org.hibernate.sqm.query.select.SqmSelectClause;

/**
 * @author Steve Ebersole
 */
public class OrderByNavigableReferenceBuilderContextImpl extends AbstractNavigableReferenceBuilderContext {
	private final SqmSelectClause sqmSelectClause;

	public OrderByNavigableReferenceBuilderContextImpl(
			SqmSelectClause sqmSelectClause,
			SemanticQueryBuilder semanticQueryBuilder) {
		super( semanticQueryBuilder );
		this.sqmSelectClause = sqmSelectClause;
	}


}
