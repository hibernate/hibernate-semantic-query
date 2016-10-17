/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.select;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.query.expression.SqmExpression;

import org.jboss.logging.Logger;

import static org.hibernate.sqm.query.select.SqmDynamicInstantiationTarget.Nature.CLASS;
import static org.hibernate.sqm.query.select.SqmDynamicInstantiationTarget.Nature.LIST;
import static org.hibernate.sqm.query.select.SqmDynamicInstantiationTarget.Nature.MAP;

/**
 * Represents a dynamic instantiation ({@code select new XYZ(...) ...}) as part of the SQM.
 *
 * @author Steve Ebersole
 */
public class SqmDynamicInstantiation
		implements SqmExpression, SqmAliasedExpressionContainer<SqmDynamicInstantiationArgument> {
	private static final Logger log = Logger.getLogger( SqmDynamicInstantiation.class );

	public static SqmDynamicInstantiation forClassInstantiation(Class targetJavaType) {
		return new SqmDynamicInstantiation( new DynamicInstantiationTargetImpl( CLASS, targetJavaType ) );
	}

	public static SqmDynamicInstantiation forMapInstantiation() {
		return new SqmDynamicInstantiation( new DynamicInstantiationTargetImpl( MAP, Map.class ) );
	}

	public static SqmDynamicInstantiation forListInstantiation() {
		return new SqmDynamicInstantiation( new DynamicInstantiationTargetImpl( LIST, List.class ) );
	}

	private final SqmDynamicInstantiationTarget instantiationTarget;
	private List<SqmDynamicInstantiationArgument> arguments;

	private SqmDynamicInstantiation(SqmDynamicInstantiationTarget instantiationTarget) {
		this.instantiationTarget = instantiationTarget;
	}

	@Override
	public String asLoggableText() {
		return "<new " + instantiationTarget.getJavaType().getName() + ">";
	}

	@Override
	public DomainReference getExpressionType() {
		return null;
	}

	@Override
	public DomainReference getInferableType() {
		return null;
	}

	public SqmDynamicInstantiationTarget getInstantiationTarget() {
		return instantiationTarget;
	}

	public List<SqmDynamicInstantiationArgument> getArguments() {
		return arguments;
	}

	public void addArgument(SqmDynamicInstantiationArgument argument) {
		if ( instantiationTarget.getNature() == LIST ) {
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
		else if ( instantiationTarget.getNature() == MAP ) {
			// must(?) have an alias...
			log.warnf(
					"Argument [%s] for dynamic Map instantiation did not declare an 'injection alias' [%s] " +
							"but such aliases are needed for dynamic Map instantiations; " +
							"will likely cause problems later translating sqm",
					argument.getExpression().toString(),
					argument.getAlias()
			);
		}

		if ( arguments == null ) {
			arguments = new ArrayList<>();
		}
		arguments.add( argument );
	}

	@Override
	public SqmDynamicInstantiationArgument add(SqmExpression expression, String alias) {
		SqmDynamicInstantiationArgument argument = new SqmDynamicInstantiationArgument( expression, alias );
		addArgument( argument );
		return argument;
	}

	@Override
	public void add(SqmDynamicInstantiationArgument aliasExpression) {
		addArgument( aliasExpression );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitDynamicInstantiation( this );
	}

	public SqmDynamicInstantiation makeShallowCopy() {
		return new SqmDynamicInstantiation( getInstantiationTarget() );
	}

	private static class DynamicInstantiationTargetImpl implements SqmDynamicInstantiationTarget {
		private final Nature nature;
		private final Class javaType;


		public DynamicInstantiationTargetImpl(Nature nature, Class javaType) {
			this.nature = nature;
			this.javaType = javaType;
		}

		@Override
		public Nature getNature() {
			return nature;
		}

		@Override
		public Class getJavaType() {
			return javaType;
		}
	}
}
