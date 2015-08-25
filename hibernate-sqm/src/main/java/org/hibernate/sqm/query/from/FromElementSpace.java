/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * Contract representing a "from element space", which is a particular root FromElement along with a list of
 * its related joins.  A list is used specifically because the order is important (!) in terms of left/right.
 *
 * @author Steve Ebersole
 */
public class FromElementSpace {
	private static final Logger log = Logger.getLogger( FromElementSpace.class );

	private final FromClause fromClause;

	private RootEntityFromElement root;
	private List<JoinedFromElement> joins;

	public FromElementSpace(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	public FromClause getFromClause() {
		return fromClause;
	}

	public RootEntityFromElement getRoot() {
		return root;
	}

	public void setRoot(RootEntityFromElement root) {
		if ( this.root != null ) {
			// we already had a root defined...
			if ( this.root == root ) {
				// its the same object reference, so no worries
				return;
			}
			else {
				// todo : error or warning?
				log.warn( "FromElementSpace#setRoot called when a root was already defined" );
			}
		}
		this.root = root;
	}

	public List<JoinedFromElement> getJoins() {
		return joins == null ? Collections.<JoinedFromElement>emptyList() : joins;
	}

	public void addJoin(JoinedFromElement join) {
		if ( joins == null ) {
			joins = new ArrayList<JoinedFromElement>();
		}
		joins.add( join );
	}
}
