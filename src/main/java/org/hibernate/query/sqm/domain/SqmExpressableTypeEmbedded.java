/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import org.hibernate.persister.embedded.spi.EmbeddedValuedNavigable;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEmbeddable;

/**
 * @author Steve Ebersole
 *
 * @deprecated {@link EmbeddedValuedNavigable}
 */
@Deprecated
public interface SqmExpressableTypeEmbedded extends SqmExpressableType, SqmNavigableSource, SqmLoggable {
	@Override
	SqmDomainTypeEmbeddable getExportedDomainType();
}
