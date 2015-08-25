/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * Represents an reference to an entity type
 *
 * @author Steve Ebersole
 */
public class EntityTypeExpression implements Expression {
	private final EntityTypeDescriptor entityTypeDescriptor;

	public EntityTypeExpression(EntityTypeDescriptor entityTypeDescriptor) {
		this.entityTypeDescriptor = entityTypeDescriptor;
	}

	@Override
	public EntityTypeDescriptor getTypeDescriptor() {
		return entityTypeDescriptor;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitEntityTypeExpression( this );
	}
}
