/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.spi;

import java.util.Set;

import org.hibernate.orm.persister.common.spi.OrmSingularAttribute;
import org.hibernate.orm.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public interface IdClassDescriptor<I> {
	Type getType();

	Set<OrmSingularAttribute<I,?>> getAttributes();
}
