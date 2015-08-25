/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class TreatedJoinedFromElement extends TreatedFromElement implements JoinedFromElement {
	public TreatedJoinedFromElement(JoinedFromElement wrapped, TypeDescriptor treatedAs) {
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
