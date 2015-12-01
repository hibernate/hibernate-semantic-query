/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

	private Map<String,FromElement> fromElementsByPath = new HashMap<String, FromElement>();

	public FromElement findFromElementWithAttribute(FromClauseStackNode fromClause, String name) {
		FromElement found = null;
		for ( FromElementSpace space : fromClause.getFromClause().getFromElementSpaces() ) {
			if ( space.getRoot().resolveAttribute( name ) != null ) {
				if ( found != null ) {
					throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
				}
				found = space.getRoot();
			}

			for ( JoinedFromElement join : space.getJoins() ) {
				if ( join.resolveAttribute( name ) != null ) {
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

	public void registerRootFromClauseStackNode(FromClauseStackNode root) {
		if ( roots == null ) {
			roots = new ArrayList<FromClauseStackNode>();
		}
		roots.add( root );
	}

	public List<FromClauseStackNode> getFromClauseStackNodeList() {
		if ( roots == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( roots );
		}
	}
}
