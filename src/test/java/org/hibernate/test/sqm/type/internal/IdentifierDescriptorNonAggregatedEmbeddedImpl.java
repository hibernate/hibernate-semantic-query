/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import java.util.Collections;
import java.util.Set;

import org.hibernate.sqm.NotYetImplementedException;

import org.hibernate.test.sqm.type.spi.IdClassDescriptor;
import org.hibernate.test.sqm.type.spi.IdentifierDescriptorNonAggregatedEmbedded;
import org.hibernate.test.sqm.type.spi.SingularAttribute;
import org.hibernate.test.sqm.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public class IdentifierDescriptorNonAggregatedEmbeddedImpl implements IdentifierDescriptorNonAggregatedEmbedded {
	// NOTE : JPA requires these to have an IdClass, so we work with that assumption here for testing
	private final IdClassDescriptor idClassDescriptor;


	public IdentifierDescriptorNonAggregatedEmbeddedImpl(Type idClassType) {
		this.idClassDescriptor = new IdClassDescriptorImpl( idClassType );
	}

	@Override
	public Set<SingularAttribute> getIdentifierAttributes() {
		throw new NotYetImplementedException(  );
	}

	@Override
	public IdClassDescriptor getIdClassDescriptor() {
		return idClassDescriptor;
	}

	static class IdClassDescriptorImpl implements IdClassDescriptor {
		private final Type idClassType;
		private final Set<SingularAttribute> idClassAttributes = Collections.emptySet();

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
