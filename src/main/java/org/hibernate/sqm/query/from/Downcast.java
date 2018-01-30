/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.EntityDescriptor;

/**
 * Models information about a downcast (TREAT AS).
 *
 * @author Steve Ebersole
 */
public class Downcast {
	private final EntityDescriptor downcastTarget;

	public Downcast(EntityDescriptor downcastTarget) {
		this.downcastTarget = downcastTarget;
	}

	public EntityDescriptor getTargetType() {
		return downcastTarget;
	}
}
