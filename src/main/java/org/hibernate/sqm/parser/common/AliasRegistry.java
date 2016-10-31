/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.hibernate.sqm.parser.AliasCollisionException;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.select.SqmSelection;

/**
 * @author Andrea Boriero
 */
public class AliasRegistry {
	private Map<String, DomainReferenceBinding> fromBindingsByAlias = new HashMap<>();
	private Map<String, SqmSelection> selectionsByAlias = new HashMap<>();

	private AliasRegistry parent;

	public AliasRegistry() {
	}

	public AliasRegistry(AliasRegistry parent) {
		this.parent = parent;
	}

	public AliasRegistry getParent() {
		return parent;
	}

	public void registerAlias(SqmSelection selection) {
		if ( selection.getAlias() != null ) {
			checkResultVariable( selection );
			selectionsByAlias.put( selection.getAlias(), selection );
		}
	}

	public void registerAlias(DomainReferenceBinding binding) {
		final DomainReferenceBinding old = fromBindingsByAlias.put( binding.getFromElement().getIdentificationVariable(), binding );
		if ( old != null ) {
			throw new AliasCollisionException(
					String.format(
							Locale.ENGLISH,
							"Alias [%s] used for multiple from-clause-elements : %s, %s",
							binding.getFromElement().getIdentificationVariable(),
							old,
							binding
					)
			);
		}
	}

	public SqmSelection findSelectionByAlias(String alias) {
		return selectionsByAlias.get( alias );
	}

	public DomainReferenceBinding findFromElementByAlias(String alias) {
		if ( fromBindingsByAlias.containsKey( alias ) ) {
			return fromBindingsByAlias.get( alias );
		}
		else if ( parent != null ) {
			return parent.findFromElementByAlias( alias );
		}
		return null;
	}

	private void checkResultVariable(SqmSelection selection) {
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
		final DomainReferenceBinding fromElement = fromBindingsByAlias.get( alias );
		if ( fromElement != null ) {
			if ( !selection.getExpression().getExpressionType().equals( fromElement.getBoundDomainReference() ) ) {
				throw new AliasCollisionException(
						String.format(
								Locale.ENGLISH,
								"Alias [%s] used in select-clause for %s is also used in from element: %s for %s",
								alias,
								selection.getExpression().getExpressionType(),
								fromElement,
								fromElement.getBoundDomainReference()
						)
				);
			}
		}
	}

}
