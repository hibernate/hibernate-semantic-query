/*
* Hibernate, Relational Persistence for Idiomatic Java
*
* License: Apache License, Version 2.0
* See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
*/
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.select.Selection;

/**
 * @author Andrea Boriero
 */
public class ResultVariableReferenceExpression implements AttributePathPart,Expression {
	final private Selection selection;

	public ResultVariableReferenceExpression(Selection selection) {
		this.selection = selection;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return selection.getExpression().getTypeDescriptor();
	}

	public Selection getUnderlyingSelection(){
		return selection;
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitResultVariableReferenceExpression(this);
	}
}

