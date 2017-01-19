/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.internal;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Value;
import org.hibernate.orm.persister.OrmTypeHelper;
import org.hibernate.orm.persister.common.internal.SingularAttributeBasic;
import org.hibernate.orm.persister.common.internal.SingularAttributeEmbedded;
import org.hibernate.orm.persister.common.internal.PersisterHelper;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.common.spi.Table;
import org.hibernate.orm.persister.entity.spi.EntityHierarchy;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.entity.spi.IdentifierDescriptor;
import org.hibernate.orm.persister.entity.spi.InheritanceStrategy;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class EntityHierarchyImpl implements EntityHierarchy {
	private final EntityPersister rootEntityPersister;
	private IdentifierDescriptor identifierDescriptor;

	public EntityHierarchyImpl(
			PersisterCreationContext creationContext,
			RootClass rootEntityBinding,
			EntityPersister rootEntityPersister) {
		this.rootEntityPersister = rootEntityPersister;

		final Table identifierTable = resolveIdentifierTable( creationContext, rootEntityBinding );
		this.identifierDescriptor = interpretIdentifierDescriptor(
				this,
				rootEntityPersister,
				creationContext,
				rootEntityBinding,
				identifierTable
		);
	}

	private static Table resolveIdentifierTable(
			PersisterCreationContext creationContext,
			RootClass rootEntityBinding) {
//		final JdbcEnvironment jdbcEnvironment = creationContext.getSessionFactory()
//				.getJdbcServices()
//				.getJdbcEnvironment();
		final org.hibernate.mapping.Table mappingTable = rootEntityBinding.getIdentityTable();
		if ( mappingTable.getSubselect() != null ) {
			return creationContext.getDatabaseModel().findDerivedTable( mappingTable.getSubselect() );
		}
		else {
//			final String name = jdbcEnvironment.getQualifiedObjectNameFormatter().format(
//					mappingTable.getQualifiedTableName(),
//					jdbcEnvironment.getDialect()
//			);
			final String name = mappingTable.getQualifiedTableName().render();
			return creationContext.getDatabaseModel().findPhysicalTable( name );

		}
	}

	private static IdentifierDescriptor interpretIdentifierDescriptor(
			EntityHierarchyImpl hierarchy,
			EntityPersister rootEntityPersister,
			PersisterCreationContext creationContext,
			RootClass rootEntityBinding,
			Table identifierTable) {
		final KeyValue identifierValueMapping = rootEntityBinding.getIdentifier();

		final List<Column> idColumns = resolveColumns( identifierTable, identifierValueMapping, creationContext );

		if ( identifierValueMapping.getType() instanceof org.hibernate.type.BasicType ) {
			return new IdentifierDescriptorSimpleImpl(
					hierarchy.getRootEntityPersister(),
					rootEntityBinding.getIdentifierProperty().getName(),
					OrmTypeHelper.convertBasic(
							(org.hibernate.type.BasicType) identifierValueMapping.getType(),
							creationContext.getTypeConfiguration()
					),
					idColumns
			);
		}
		else {
			final CompositeType cidType = (CompositeType) identifierValueMapping.getType();
			// todo : need to pass along that any built sub attributes are part of the id
			if ( rootEntityBinding.hasIdentifierProperty() ) {
				return new IdentifierDescriptorAggregatedEmbeddedImpl(
						(SingularAttributeEmbedded) PersisterHelper.INSTANCE.buildSingularAttribute(
								creationContext,
								// the declaring type...
								///		for now we use the root entity
								hierarchy.getRootEntityPersister(),
								// value?
								null,
								rootEntityBinding.getIdentifierProperty().getName(),
								OrmTypeHelper.convertComposite(
										creationContext,
										rootEntityBinding.getIdentifierProperty().getName(),
										(Component) identifierValueMapping,
										hierarchy.getRootEntityPersister(),
										creationContext.getTypeConfiguration()
								),
								idColumns
						)
				);
			}
			else {
				// todo : pass info about ther IdClass
				return new IdentifierDescriptorNonAggregatedEmbeddedImpl(
						rootEntityPersister,
						OrmTypeHelper.convertComposite(
								creationContext,
								"<id>",
								(Component) identifierValueMapping,
								hierarchy.getRootEntityPersister(),
								creationContext.getTypeConfiguration()
						),
						OrmTypeHelper.convertComposite(
								creationContext,
								"<IdClass>",
								rootEntityBinding.getIdentifierMapper(),
								hierarchy.getRootEntityPersister(),
								creationContext.getTypeConfiguration()
						)
				);

//						PersisterHelper.INSTANCE.buildEmbeddablePersister(
//								creationContext,
//								hierarchy.getRootEntityPersister(),
//								rootEntityBinding.getEntityName() + ".id",
//								cidType,
//								idColumns
//						)
			}
		}
	}

	private static List<Column> resolveColumns(
			Table table,
			Value value,
			PersisterCreationContext creationContext) {
		final String[] columnNames = new String[value.getColumnSpan()];
		final String[] formulas = value.hasFormula() ? new String[value.getColumnSpan()] : null;
		//final SqlTypeDescriptor[] sqlTypeDescriptors = new SqlTypeDescriptor[value.getColumnSpan()];
		final int[] jdbcTypeCodes = new int[value.getColumnSpan()];

		final Iterator<Selectable> itr = value.getColumnIterator();
		int i = 0;
		while ( itr.hasNext() ) {
			final Selectable selectable = itr.next();
			if ( selectable instanceof org.hibernate.mapping.Column ) {
//				columnNames[i] = ( (org.hibernate.mapping.Column) selectable ).getQuotedName(
//						creationContext.getSessionFactory().getJdbcServices().getJdbcEnvironment().getDialect()
//				);
				columnNames[i] = '`' + ( (org.hibernate.mapping.Column) selectable ).getName() + '`';
			}
			else {
				if ( formulas == null ) {
					throw new HibernateException(
							"Value indicated it does not have formulas, but a formula was encountered : " + selectable );
				}
				formulas[i] = ( (Formula) selectable ).getFormula();
			}

			// todo : need access to the TypeConfiguration...
			//sqlTypeDescriptors[i] = creationContext.getSessionFactory()
			jdbcTypeCodes[i] = value.getType().sqlTypes( null )[i];

			// todo : keep track of readers/writers... how exactly?
			// something like this vv ?
			//		Column#applyReadExpression( col.getReadExpr( dialect ) )
			//		Column#applyWriteExpression( col.getWriteExpr() )

			i++;
		}

		// makeColumns(
		//		creationContext,
		//		tableSelector,
		//		columnNames,
		//		formulas,
		//		sqlTypeDescriptors (or just JDBC type codes?)
		// )
		return PersisterHelper.makeValues(
				creationContext,
				// todo : a Table "selector"...
				table,
				columnNames,
				formulas,
				jdbcTypeCodes
		);
	}

	@Override
	public EntityPersister getRootEntityPersister() {
		return rootEntityPersister;
	}

	@Override
	public InheritanceStrategy getInheritanceStrategy() {
		return InheritanceStrategy.NONE;
	}

	@Override
	public IdentifierDescriptor getIdentifierDescriptor() {
		return identifierDescriptor;
	}

	@Override
	public SingularAttributeBasic getVersionAttribute() {
		return null;
	}

	@Override
	public void finishInitialization(PersisterCreationContext creationContext, RootClass mappingType) {
		// todo : identifierDescriptor init, etc
	}
}
