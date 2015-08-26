/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;

/**
 * @author Steve Ebersole
 */
public class SubQueryExpression implements Expression {
	private final QuerySpec querySpec;
	private final TypeDescriptor typeDescriptor;

	public SubQueryExpression(QuerySpec querySpec) {
		this.querySpec = querySpec;
		this.typeDescriptor = determineTypeDescriptor( querySpec.getSelectClause() );
	}

	private static TypeDescriptor determineTypeDescriptor(SelectClause selectClause) {
		if ( selectClause.getSelections().size() != 0 ) {
			return null;
		}

		final Selection selection = selectClause.getSelections().get( 0 );
		return selection.getExpression().getTypeDescriptor();
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	public QuerySpec getQuerySpec() {
		return querySpec;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSubQueryExpression( this );
	}
}
