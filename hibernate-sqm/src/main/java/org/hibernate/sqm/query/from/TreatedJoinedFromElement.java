/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.query.JoinType;

/**
 * @author Steve Ebersole
 */
public class TreatedJoinedFromElement extends TreatedFromElement implements JoinedFromElement {
	public TreatedJoinedFromElement(JoinedFromElement wrapped, EntityType treatedAs) {
		super( wrapped, treatedAs );
	}

	@Override
	public JoinedFromElement getWrapped() {
		return (JoinedFromElement) super.getWrapped();
	}

	@Override
	public JoinType getJoinType() {
		return getWrapped().getJoinType();
	}
}
