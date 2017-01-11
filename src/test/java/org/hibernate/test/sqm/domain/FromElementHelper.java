/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class FromElementHelper {
	public static SqmFrom extractSourceFromElement(SqmExpression expression) {
		if ( expression instanceof SqmNavigableBinding ) {
			SqmNavigableBinding navigableBinding = (SqmNavigableBinding) expression;
			if ( navigableBinding.getSourceBinding() != null ) {
				return navigableBinding.getSourceBinding().getExportedFromElement();
			}
			else if ( navigableBinding instanceof SqmNavigableSourceBinding ) {
				return ( (SqmNavigableSourceBinding) navigableBinding ).getExportedFromElement();
			}

			return null;
		}

		// any other types of expressions we know how to get the SqmFrom element from?

		throw new RuntimeException( "Don't know how to extract SqmFrom element from given SqmExpression : " + expression );
	}

	public static SqmFrom extractExpressionFromElement(SqmExpression expression) {
		if ( expression instanceof SqmNavigableSourceBinding ) {
			return ( (SqmNavigableSourceBinding) expression ).getExportedFromElement();
		}
		else if ( expression instanceof SqmNavigableBinding ) {
			final SqmNavigableBinding navigableBinding = (SqmNavigableBinding) expression;
			if ( navigableBinding.getSourceBinding() != null ) {
				return navigableBinding.getSourceBinding().getExportedFromElement();
			}

			return null;
		}

		// any other types of expressions we know how to get the SqmFrom element from?

		throw new RuntimeException( "Don't know how to extract SqmFrom element from given SqmExpression : " + expression );
	}

	public static SqmFrom extractExpressionExportedFromElement(SqmNavigableBinding expression) {
		if ( expression instanceof SqmNavigableSourceBinding ) {
			return ( (SqmNavigableSourceBinding) expression ).getExportedFromElement();
		}
		return null;
	}
}
