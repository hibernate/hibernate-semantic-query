/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import java.util.Set;

/**
 * Base information describing an identifier which can not be referenced through a single attribute
 *
 * @author Steve Ebersole
 */
public interface IdentifierDescriptorMultipleAttribute {
	interface IdClassDescriptor {
		Type getType();
		Set<SingularAttribute> getAttributes();
	}

	Set<SingularAttribute> getIdentifierAttributes();
	IdClassDescriptor getIdClassDescriptor();
}
