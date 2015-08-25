/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query;

import java.util.Locale;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.predicate.WhereClauseContainer;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class DeleteStatement implements Statement, WhereClauseContainer {
	private static final Logger log = Logger.getLogger( DeleteStatement.class );

	private RootEntityFromElement entityFromElement;
	private WhereClause whereClause = new WhereClause();

	@Override
	public Type getType() {
		return Type.DELETE;
	}

	public RootEntityFromElement getEntityFromElement() {
		return entityFromElement;
	}

	public void setEntityFromElement(RootEntityFromElement entityFromElement) {
		if ( this.entityFromElement != null ) {
			// the entity reference was already defined...
			if ( this.entityFromElement != entityFromElement ) {
				log.debugf(
						"DeleteStatement#entityFromElement set more than once : %s, %s",
						this.entityFromElement,
						entityFromElement
				);
			}

		}
		this.entityFromElement = entityFromElement;
	}

	@Override
	public WhereClause getWhereClause() {
		return whereClause;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"delete %s %s",
				entityFromElement,
				whereClause
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitDeleteStatement( this );
	}
}
