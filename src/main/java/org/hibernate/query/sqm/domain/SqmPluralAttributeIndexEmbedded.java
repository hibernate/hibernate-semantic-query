/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

/**
 * @author Steve Ebersole
 *
 * @deprecated {@link org.hibernate.persister.collection.spi.CollectionIndexEmbedded}
 */
@Deprecated
public interface SqmPluralAttributeIndexEmbedded extends SqmPluralAttributeIndex, SqmExpressableTypeEmbedded {
	@Override
	default IndexClassification getClassification() {
		return IndexClassification.EMBEDDABLE;
	}
}
