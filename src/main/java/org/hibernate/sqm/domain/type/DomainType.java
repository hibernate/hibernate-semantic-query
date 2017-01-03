/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.domain.type;

import org.hibernate.sqm.domain.Loggable;

/**
 * Represents any "type" used in the domain model.
 * <p/>
 * Think {@code org.hibernate.type.spi.Type} from Hibernate ORM.
 * <p/>
 * Mainly used to represent a singular memento we can embed into the SQM tree
 * for the consumer to be able to map back to its "type system"
 *
 * @author Steve Ebersole
 */
public interface DomainType extends Loggable {
}
