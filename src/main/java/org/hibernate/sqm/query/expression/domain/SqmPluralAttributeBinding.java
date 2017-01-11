/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.SqmPluralAttribute;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * @author Steve Ebersole
 */
public class SqmPluralAttributeBinding
		extends AbstractSqmAttributeBinding<SqmPluralAttribute>
		implements SqmNavigableSourceBinding {
	public SqmPluralAttributeBinding(SqmNavigableSourceBinding lhs, SqmPluralAttribute attribute) {
		super( lhs, attribute );
	}

	public SqmPluralAttributeBinding(SqmAttributeJoin join) {
		super( join );
	}

	@Override
	public SqmPluralAttribute getBoundNavigable() {
		return super.getBoundNavigable();
	}

	@Override
	public SqmAttributeJoin getExportedFromElement() {
		return (SqmAttributeJoin) super.getExportedFromElement();
	}


}
