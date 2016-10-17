/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

/**
 * Base information describing an identifier which can be referenced through a single attribute
 *
 * @author Steve Ebersole
 */
public interface IdentifierDescriptorSingleAttribute extends IdentifierDescriptor {
	SingularAttribute getIdAttribute();
}
