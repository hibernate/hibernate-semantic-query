/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.sqm.domain.IdentifierDescriptorMultipleAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class NonAggregatedCompositeIdentifierDescriptor implements IdentifierDescriptorMultipleAttribute {
	// NOTE : JPA requires these to have an IdClass, so we work with that assumption here for testing
	private final IdClassDescriptor idClassDescriptor;


	public NonAggregatedCompositeIdentifierDescriptor(Type idClassType) {
		this.idClassDescriptor = new IdClassDescriptorImpl( idClassType );
	}

	@Override
	public Set<SingularAttribute> getIdentifierAttributes() {
		return null;
	}

	@Override
	public IdClassDescriptor getIdClassDescriptor() {
		return idClassDescriptor;
	}

	static class IdClassDescriptorImpl implements IdClassDescriptor {
		private final Type idClassType;
		private final Set<SingularAttribute> idClassAttributes = new HashSet<SingularAttribute>();

		public IdClassDescriptorImpl(Type idClassType) {
			this.idClassType = idClassType;
		}

		@Override
		public Type getType() {
			return idClassType;
		}

		@Override
		public Set<SingularAttribute> getAttributes() {
			return idClassAttributes;
		}
	}
}
