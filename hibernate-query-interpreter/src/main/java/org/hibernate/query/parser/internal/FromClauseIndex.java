/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.query.parser.internal.hql.phase1.FromClauseStackNode;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;

import org.jboss.logging.Logger;

/**
 * Maintains numerous indexes over information and state determined during the Phase 1 processing of
 * a queries from clauses.
 *
 * @author Steve Ebersole
 */
public class FromClauseIndex {
	private static final Logger log = Logger.getLogger( FromClauseIndex.class );

	private List<FromClauseStackNode> roots;

	private Map<String,FromElement> fromElementsByAlias = new HashMap<String, FromElement>();
	private Map<String,FromElement> fromElementsByPath = new HashMap<String, FromElement>();

	public void registerAlias(FromElement fromElement) {
		final FromElement old = fromElementsByAlias.put( fromElement.getAlias(), fromElement );
		if ( old != null ) {
			throw new IllegalStateException(
					String.format(
							Locale.ENGLISH,
							"Alias [%s] used for multiple from-clause-elements : %s, %s",
							fromElement.getAlias(),
							old,
							fromElement
					)
			);
		}
	}

	public FromElement findFromElementByAlias(String alias) {
		return fromElementsByAlias.get( alias );
	}

	public FromElement findFromElementWithAttribute(FromClauseStackNode fromClause, String name) {
		FromElement found = null;
		for ( FromElementSpace space : fromClause.getFromClause().getFromElementSpaces() ) {
			if ( space.getRoot().getTypeDescriptor().getAttributeDescriptor( name ) != null ) {
				if ( found != null ) {
					throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
				}
				found = space.getRoot();
			}

			for ( JoinedFromElement join : space.getJoins() ) {
				if ( join.getTypeDescriptor().getAttributeDescriptor( name ) != null ) {
					if ( found != null ) {
						throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
					}
					found = join;
				}
			}
		}

		if ( found == null ) {
			if ( fromClause.hasParent() ) {
				log.debugf( "Unable to resolve unqualified attribute [%s] in local FromClause; checking parent" );
				found = findFromElementWithAttribute( fromClause.getParentNode(), name );
			}
		}

		return found;
	}

	public void registerRootFromClauseNode(FromClauseStackNode root) {
		if ( roots == null ) {
			roots = new ArrayList<FromClauseStackNode>();
		}
		roots.add( root );
	}

	public List<FromClauseStackNode> getRootFromClauseNodeList() {
		if ( roots == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( roots );
		}
	}
}
