/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.spi;

import org.hibernate.sqm.domain.SingularSqmAttributeAnyReference;

/**
 * Reference to a singular attribute whose value (DomainType) is an any-mapping
 *
 * @author Steve Ebersole
 */
public interface SingularAttributeAny extends SingularAttribute, SingularSqmAttributeAnyReference {
}
