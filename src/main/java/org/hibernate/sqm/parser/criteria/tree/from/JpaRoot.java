/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree.from;

import javax.persistence.criteria.Root;

import org.hibernate.sqm.domain.EntityReference;

/**
 * @author Steve Ebersole
 */
public interface JpaRoot<X> extends Root<X>, JpaFrom<X,X> {
	EntityReference getEntityType();
}
