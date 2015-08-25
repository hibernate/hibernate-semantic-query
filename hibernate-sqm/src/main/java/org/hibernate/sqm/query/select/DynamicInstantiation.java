/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.BasicTypeDescriptor;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class DynamicInstantiation implements Selection, Expression {
	private final Class instantiationTarget;
	private final BasicTypeDescriptor typeDescriptor;

	private List<AliasedDynamicInstantiationArgument> aliasedArguments;

	public DynamicInstantiation(Class instantiationTarget) {
		this.instantiationTarget = instantiationTarget;
		this.typeDescriptor = new InstantiationTypeDescriptor( instantiationTarget );
	}

	public DynamicInstantiation(
			final Class instantiationTarget,
			List<AliasedDynamicInstantiationArgument> aliasedArguments) {
		this( instantiationTarget );
		this.aliasedArguments = aliasedArguments;
	}

	public DynamicInstantiation(
			Class instantiationTarget,
			AliasedDynamicInstantiationArgument... aliasedArguments) {
		this( instantiationTarget, Arrays.asList( aliasedArguments ) );
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	public Class getInstantiationTarget() {
		return instantiationTarget;
	}

	public List<AliasedDynamicInstantiationArgument> getAliasedArguments() {
		return aliasedArguments;
	}

	public void addArgument(AliasedDynamicInstantiationArgument argument) {
		if ( aliasedArguments == null ) {
			aliasedArguments = new ArrayList<AliasedDynamicInstantiationArgument>();
		}
		aliasedArguments.add( argument );
	}

	public void addArgument(Expression argument, String alias) {
		addArgument( new AliasedDynamicInstantiationArgument( argument, alias ) );
	}

	public void addArgument(Expression argument) {
		addArgument( argument, null );
	}

	private static class InstantiationTypeDescriptor implements BasicTypeDescriptor {
		private final Class instantiationTarget;

		public InstantiationTypeDescriptor(Class instantiationTarget) {
			this.instantiationTarget = instantiationTarget;
		}

		@Override
		public Class getCorrespondingJavaType() {
			return instantiationTarget;
		}

		@Override
		public String getTypeName() {
			return instantiationTarget.getName();
		}

		@Override
		public AttributeDescriptor getAttributeDescriptor(String attributeName) {
			return null;
		}
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitDynamicInstantiation( this );
	}
}
