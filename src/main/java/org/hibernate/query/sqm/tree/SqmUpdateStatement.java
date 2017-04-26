/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree;

import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.set.SqmSetClause;

/**
 * @author Steve Ebersole
 */
public interface SqmUpdateStatement extends SqmNonSelectStatement{
	SqmRoot getEntityFromElement();
	SqmSetClause getSetClause();
}
