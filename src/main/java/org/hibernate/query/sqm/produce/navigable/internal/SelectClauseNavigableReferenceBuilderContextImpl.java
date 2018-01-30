/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.navigable.internal;

import org.hibernate.query.sqm.produce.navigable.spi.AbstractNavigableReferenceBuilderContext;
import org.hibernate.query.sqm.produce.spi.SemanticQueryBuilder;

/**
 * @author Steve Ebersole
 */
public class SelectClauseNavigableReferenceBuilderContextImpl extends AbstractNavigableReferenceBuilderContext {
	public SelectClauseNavigableReferenceBuilderContextImpl(SemanticQueryBuilder semanticQueryBuilder) {
		super( semanticQueryBuilder );
	}

	@Override
	public boolean forceTerminalJoin() {
		return true;
	}
}
