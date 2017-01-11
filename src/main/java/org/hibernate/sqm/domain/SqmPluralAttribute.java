/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import javax.persistence.metamodel.PluralAttribute;

/**
 * Models references to plural attributes (persistent collections)
 *
 * @author Steve Ebersole
 */
public interface SqmPluralAttribute extends SqmAttribute, SqmDomainTypeExporter, SqmNavigableSource {
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
