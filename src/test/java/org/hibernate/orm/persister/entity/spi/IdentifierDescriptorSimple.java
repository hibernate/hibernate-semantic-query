/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.spi;

import org.hibernate.orm.persister.common.internal.SingularAttributeBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityIdentifierBasic;

/**
 * @author Steve Ebersole
 */
public interface IdentifierDescriptorSimple extends IdentifierDescriptorSingleAttribute, SqmEntityIdentifierBasic {
	@Override
	SingularAttributeBasic getIdAttribute();
}
