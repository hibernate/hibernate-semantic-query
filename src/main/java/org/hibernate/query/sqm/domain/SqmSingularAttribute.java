/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

/**
 * Models references to singular attributes (basic, any, embeddable, one-to-one, many-to-one).
 *
 * @author Steve Ebersole
 *
 * @deprecated {@link org.hibernate.persister.common.spi.SingularPersistentAttribute}
 */
@Deprecated
public interface SqmSingularAttribute<T> extends SqmAttribute<T>, SqmExpressableType<T> {
	/**
	 * Classifications of the singularity
	 */
	enum SingularAttributeClassification {
		BASIC,
		EMBEDDED,
		ANY,
		ONE_TO_ONE,
		MANY_TO_ONE
	}

	/**
	 * Obtain the classification enum for the attribute.
	 *
	 * @return The classification
	 */
	SingularAttributeClassification getAttributeTypeClassification();
}
