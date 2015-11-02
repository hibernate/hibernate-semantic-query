/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.BasicTypeDescriptor;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.expression.Expression;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class DynamicInstantiation implements Expression, AliasedExpressionContainer<DynamicInstantiationArgument> {
	private static final Logger log = Logger.getLogger( DynamicInstantiation.class );

	public static DynamicInstantiation forClassInstantiation(Class type) {
		return new DynamicInstantiation(
				new DynamicInstantiationTargetImpl(
						DynamicInstantiationTarget.Nature.CLASS,
						new ClassInstantiationTypeDescriptor( type )
				)
		);
	}

	public static DynamicInstantiation forMapInstantiation() {
		return new DynamicInstantiation(
				new DynamicInstantiationTargetImpl(
						DynamicInstantiationTarget.Nature.MAP,
						StandardBasicTypeDescriptors.INSTANCE.MAP
				)
		);
	}

	public static DynamicInstantiation forListInstantiation() {
		return new DynamicInstantiation(
				new DynamicInstantiationTargetImpl(
						DynamicInstantiationTarget.Nature.LIST,
						StandardBasicTypeDescriptors.INSTANCE.LIST
				)
		);
	}

	private final DynamicInstantiationTarget instantiationTarget;
	private List<DynamicInstantiationArgument> arguments;

	private DynamicInstantiation(DynamicInstantiationTarget instantiationTarget) {
		this.instantiationTarget = instantiationTarget;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return instantiationTarget.getTargetJavaType();
	}

	public DynamicInstantiationTarget getInstantiationTarget() {
		return instantiationTarget;
	}

	public List<DynamicInstantiationArgument> getArguments() {
		return arguments;
	}

	public void addArgument(DynamicInstantiationArgument argument) {
		if ( instantiationTarget.getNature() == DynamicInstantiationTarget.Nature.LIST ) {
			// really should not have an alias...
			if ( argument.getAlias() != null ) {
				log.debugf(
						"Argument [%s] for dynamic List instantiation declared an 'injection alias' [%s] " +
								"but such aliases are ignored for dynamic List instantiations",
						argument.getExpression().toString(),
						argument.getAlias()
				);
			}
		}
		else if ( instantiationTarget.getNature() == DynamicInstantiationTarget.Nature.MAP ) {
			// must(?) have an alias...
			log.warnf(
					"Argument [%s] for dynamic Map instantiation did not declare an 'injection alias' [%s] " +
							"but such aliases are needed for dynamic Map instantiations; " +
							"will likely cause problems later translating query",
					argument.getExpression().toString(),
					argument.getAlias()
			);
		}

		if ( arguments == null ) {
			arguments = new ArrayList<DynamicInstantiationArgument>();
		}
		arguments.add( argument );
	}

	@Override
	public DynamicInstantiationArgument add(Expression expression, String alias) {
		DynamicInstantiationArgument argument = new DynamicInstantiationArgument( expression, alias );
		addArgument( argument );
		return argument;
	}

	@Override
	public void add(DynamicInstantiationArgument aliasExpression) {
		addArgument( aliasExpression );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitDynamicInstantiation( this );
	}

	public DynamicInstantiation makeShallowCopy() {
		return new DynamicInstantiation( getInstantiationTarget() );
	}

	private static class DynamicInstantiationTargetImpl implements DynamicInstantiationTarget {
		private final Nature nature;
		private final TypeDescriptor typeDescriptor;

		public DynamicInstantiationTargetImpl(Nature nature, TypeDescriptor typeDescriptor) {
			this.nature = nature;
			this.typeDescriptor = typeDescriptor;
		}

		@Override
		public Nature getNature() {
			return nature;
		}

		@Override
		public TypeDescriptor getTargetJavaType() {
			return typeDescriptor;
		}
	}

	private static class ClassInstantiationTypeDescriptor implements BasicTypeDescriptor {
		private final Class instantiationTarget;

		public ClassInstantiationTypeDescriptor(Class instantiationTarget) {
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
			// todo : we may want to do this for "property-injection"
			return null;
		}
	}
}
