/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.PluralSqmAttributeReference;
import org.hibernate.sqm.query.from.SqmAttributeJoin;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeBinding extends AbstractAttributeBinding<PluralSqmAttributeReference> {
	public PluralAttributeBinding(DomainReferenceBinding lhs, PluralSqmAttributeReference attribute) {
		super( lhs, attribute );
	}

	public PluralAttributeBinding(
			DomainReferenceBinding lhs,
			PluralSqmAttributeReference attribute,
			SqmAttributeJoin join) {
		super( lhs, attribute, join );
	}
}
