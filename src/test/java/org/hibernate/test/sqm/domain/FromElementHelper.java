/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableSourceReference;
import org.hibernate.query.sqm.tree.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class FromElementHelper {
	public static SqmFrom extractSourceFromElement(SqmExpression expression) {
		if ( expression instanceof SqmNavigableReference ) {
			SqmNavigableReference navigableBinding = (SqmNavigableReference) expression;
			if ( navigableBinding.getSourceReference() != null ) {
				return navigableBinding.getSourceReference().getExportedFromElement();
			}
			else if ( navigableBinding instanceof SqmNavigableSourceReference ) {
				return ( (SqmNavigableSourceReference) navigableBinding ).getExportedFromElement();
			}

			return null;
		}

		// any other types of expressions we know how to get the SqmFrom element from?

		throw new RuntimeException( "Don't know how to extract SqmFrom element from given SqmExpression : " + expression );
	}

	public static SqmFrom extractExpressionFromElement(SqmExpression expression) {
		if ( expression instanceof SqmNavigableSourceReference ) {
			return ( (SqmNavigableSourceReference) expression ).getExportedFromElement();
		}
		else if ( expression instanceof SqmNavigableReference ) {
			final SqmNavigableReference navigableBinding = (SqmNavigableReference) expression;
			if ( navigableBinding.getSourceReference() != null ) {
				return navigableBinding.getSourceReference().getExportedFromElement();
			}

			return null;
		}

		// any other types of expressions we know how to get the SqmFrom element from?

		throw new RuntimeException( "Don't know how to extract SqmFrom element from given SqmExpression : " + expression );
	}

	public static SqmFrom extractExpressionExportedFromElement(SqmNavigableReference expression) {
		if ( expression instanceof SqmNavigableSourceReference ) {
			return ( (SqmNavigableSourceReference) expression ).getExportedFromElement();
		}
		return null;
	}
}
