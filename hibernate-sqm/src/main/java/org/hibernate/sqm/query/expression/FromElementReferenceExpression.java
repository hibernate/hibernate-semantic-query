/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class FromElementReferenceExpression implements AttributePathPart {
	private final FromElement fromElement;

	public FromElementReferenceExpression(FromElement fromElement) {
		this.fromElement = fromElement;
	}

	public FromElement getFromElement() {
		return fromElement;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return fromElement.getTypeDescriptor();
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		return fromElement;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitFromElementReferenceExpression( this );
	}
}
