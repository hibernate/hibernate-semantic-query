/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * Models references to singular attributes (basic, any, embeddable, one-to-one, many-to-one).
 *
 * @author Steve Ebersole
 */
public interface SingularAttributeDescriptor extends AttributeDescriptor, PotentialEntityDescriptorExporter {
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
