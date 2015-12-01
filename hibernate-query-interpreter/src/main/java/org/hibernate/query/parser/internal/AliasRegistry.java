/*
* Hibernate, Relational Persistence for Idiomatic Java
*
* License: Apache License, Version 2.0
* See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
*/
package org.hibernate.query.parser.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.hibernate.query.parser.AliasCollisionException;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.select.Selection;

/**
 * @author Andrea Boriero
 */
public class AliasRegistry {
	private Map<String, FromElement> fromElementsByAlias = new HashMap<String, FromElement>();
	private Map<String, Selection> selectionsByAlias = new HashMap<String, Selection>();

	private AliasRegistry parent;

	public AliasRegistry() {
	}

	public AliasRegistry(AliasRegistry parent) {
		this.parent = parent;
		fromElementsByAlias = new HashMap<String, FromElement>();
	}

	public AliasRegistry getParent() {
		return parent;
	}

	public void registerAlias(Selection selection) {
		if ( selection.getAlias() != null ) {
			checkResultVariable( selection );
			selectionsByAlias.put( selection.getAlias(), selection );
		}
	}

	public void registerAlias(FromElement fromElement) {
		final FromElement old = fromElementsByAlias.put( fromElement.getAlias(), fromElement );
		if ( old != null ) {
			throw new AliasCollisionException(
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

	public Selection findSelectionByAlias(String alias) {
		return selectionsByAlias.get( alias );
	}

	public FromElement findFromElementByAlias(String alias) {
		if ( fromElementsByAlias.containsKey( alias ) ) {
			return fromElementsByAlias.get( alias );
		}
		else if ( parent != null ) {
			return parent.findFromElementByAlias( alias );
		}
		return null;
	}

	private void checkResultVariable(Selection selection) {
		final String alias = selection.getAlias();
		if ( selectionsByAlias.containsKey( alias ) ) {
			throw new AliasCollisionException(
					String.format(
							Locale.ENGLISH,
							"Alias [%s] is already used in same select clause",
							alias
					)
			);
		}
		final FromElement fromElement = fromElementsByAlias.get( alias );
		if ( fromElement != null ) {
			if ( !selection.getExpression().getExpressionType().equals( fromElement.getBindableModelDescriptor() ) ) {
				throw new AliasCollisionException(
						String.format(
								Locale.ENGLISH,
								"Alias [%s] used in select-clause for %s is also used in from element: %s for %s",
								alias,
								selection.getExpression().getExpressionType(),
								fromElement,
								fromElement.getBindableModelDescriptor()
						)
				);
			}
		}
	}

}
