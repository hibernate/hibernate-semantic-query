/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.EntityReference;

/**
 * Models information about a downcast (TREAT AS).
 *
 * @author Steve Ebersole
 */
public class Downcast {
	private final EntityReference downcastTarget;

	public Downcast(EntityReference downcastTarget) {
		this.downcastTarget = downcastTarget;
	}

	public EntityReference getTargetType() {
		return downcastTarget;
	}
}
