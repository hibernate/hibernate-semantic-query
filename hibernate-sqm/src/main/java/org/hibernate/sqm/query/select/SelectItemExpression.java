/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.select;

import org.hibernate.sqm.query.expression.Expression;

/**
 * Essentially represents an aliased select expression
 *
 * @author Steve Ebersole
 */
public interface SelectItemExpression {
	Expression getSelectedExpression();
	String getSelectedAlias();
}
