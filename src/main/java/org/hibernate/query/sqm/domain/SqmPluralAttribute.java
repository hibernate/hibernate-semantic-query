/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import javax.persistence.metamodel.PluralAttribute;

/**
 * Models references to plural attributes (persistent collections)
 *
 * @author Steve Ebersole
 *
 * @deprecated {@link org.hibernate.persister.common.spi.PluralPersistentAttribute}
 */
@Deprecated
public interface SqmPluralAttribute<J> extends SqmAttribute<J>, SqmDomainTypeExporter<J>, SqmNavigableSource<J> {
	/**
	 * Classifications of the plurality
	 */
	enum CollectionClassification {
		SET( PluralAttribute.CollectionType.SET ),
		LIST( PluralAttribute.CollectionType.LIST ),
		MAP( PluralAttribute.CollectionType.MAP ),
		BAG( PluralAttribute.CollectionType.COLLECTION );

		private final PluralAttribute.CollectionType jpaClassification;

		CollectionClassification(PluralAttribute.CollectionType jpaClassification) {
			this.jpaClassification = jpaClassification;
		}

		public PluralAttribute.CollectionType toJpaClassification() {
			return jpaClassification;
		}
	}

	CollectionClassification getCollectionClassification();

	SqmPluralAttributeElement getElementReference();

	SqmPluralAttributeIndex getIndexReference();

	String getRole();
}
