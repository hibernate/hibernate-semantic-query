/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.select;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;

/**
 * @author Steve Ebersole
 */
public class SelectList implements Selection {
	private List<SelectListItem> selectListItems;

	public SelectList(SelectListItem... items) {
		if ( items != null ) {
			for ( SelectListItem item : items ) {
				addSelectListItem( item );
			}

		}
	}

	public void addSelectListItem(SelectListItem item) {
		if ( selectListItems == null ) {
			selectListItems = new ArrayList<SelectListItem>();
		}
		selectListItems.add( item );
	}

	public List<SelectListItem> getSelectListItems() {
		return selectListItems;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSelectList( this );
	}
}
