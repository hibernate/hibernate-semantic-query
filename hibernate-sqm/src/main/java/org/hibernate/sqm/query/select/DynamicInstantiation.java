/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
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
public class DynamicInstantiation implements Expression {
	private final Class instantiationTarget;
	private final BasicTypeDescriptor typeDescriptor;

	private List<DynamicInstantiationArgument> arguments;

	public DynamicInstantiation(Class instantiationTarget) {
		this.instantiationTarget = instantiationTarget;
		this.typeDescriptor = new InstantiationTypeDescriptor( instantiationTarget );
	}

	public DynamicInstantiation(
			final Class instantiationTarget,
			List<DynamicInstantiationArgument> arguments) {
		this( instantiationTarget );
		this.arguments = arguments;
	}

	public DynamicInstantiation(
			Class instantiationTarget,
			DynamicInstantiationArgument... arguments) {
		this( instantiationTarget, Arrays.asList( arguments ) );
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	public Class getInstantiationTarget() {
		return instantiationTarget;
	}

	public List<DynamicInstantiationArgument> getArguments() {
		return arguments;
	}

	public void addArgument(DynamicInstantiationArgument argument) {
		if ( arguments == null ) {
			arguments = new ArrayList<DynamicInstantiationArgument>();
		}
		arguments.add( argument );
	}

	public void addArgument(Expression argument, String alias) {
		addArgument( new DynamicInstantiationArgument( argument, alias ) );
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
