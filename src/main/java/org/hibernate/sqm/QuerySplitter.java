/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmExpressableTypeEntityPolymorphicEntity;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.parser.common.NavigableBindingHelper;
import org.hibernate.sqm.query.SqmDeleteStatement;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.SqmUpdateStatement;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.EntityTypeLiteralSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterSqmExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFalseSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFloatSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralLongSqmExpression;
import org.hibernate.sqm.query.expression.LiteralNullSqmExpression;
import org.hibernate.sqm.query.expression.LiteralStringSqmExpression;
import org.hibernate.sqm.query.expression.LiteralTrueSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableBinding;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.query.expression.domain.SqmPluralAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.ConcatFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.GenericFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MinFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.internal.SqmSelectStatementImpl;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.paging.LimitOffsetClause;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.BetweenSqmPredicate;
import org.hibernate.sqm.query.predicate.EmptinessSqmPredicate;
import org.hibernate.sqm.query.predicate.GroupedSqmPredicate;
import org.hibernate.sqm.query.predicate.InListSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.LikeSqmPredicate;
import org.hibernate.sqm.query.predicate.MemberOfSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatedSqmPredicate;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.OrSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmDynamicInstantiation;
import org.hibernate.sqm.query.select.SqmDynamicInstantiationArgument;
import org.hibernate.sqm.query.select.SqmSelectClause;
import org.hibernate.sqm.query.select.SqmSelection;
import org.hibernate.sqm.query.set.SqmAssignment;
import org.hibernate.sqm.query.set.SqmSetClause;

/**
 * Handles splitting queries containing unmapped polymorphic references.
 *
 * @author Steve Ebersole
 */
public class QuerySplitter {
	public static SqmSelectStatement[] split(SqmSelectStatement statement) {
		// We only allow unmapped polymorphism in a very restricted way.  Specifically,
		// the unmapped polymorphic reference can only be a root and can be the only
		// root.  Use that restriction to locate the unmapped polymorphic reference
		SqmRoot unmappedPolymorphicReference = null;
		for ( FromElementSpace fromElementSpace : statement.getQuerySpec().getFromClause().getFromElementSpaces() ) {
			if ( SqmExpressableTypeEntityPolymorphicEntity.class.isInstance( fromElementSpace.getRoot().getBinding().getBoundNavigable() ) ) {
				unmappedPolymorphicReference = fromElementSpace.getRoot();
			}
		}

		if ( unmappedPolymorphicReference == null ) {
			return new SqmSelectStatement[] { statement };
		}

		final SqmExpressableTypeEntityPolymorphicEntity unmappedPolymorphicDescriptor = (SqmExpressableTypeEntityPolymorphicEntity) unmappedPolymorphicReference.getBinding().getBoundNavigable();
		final SqmSelectStatement[] expanded = new SqmSelectStatement[ unmappedPolymorphicDescriptor.getImplementors().size() ];

		int i = -1;
		for ( SqmExpressableTypeEntity mappedDescriptor : unmappedPolymorphicDescriptor.getImplementors() ) {
			i++;
			final UnmappedPolymorphismReplacer replacer = new UnmappedPolymorphismReplacer(
					statement,
					unmappedPolymorphicReference,
					mappedDescriptor
			);
			expanded[i] = replacer.visitSelectStatement( statement );
		}

		return expanded;
	}

	@SuppressWarnings("unchecked")
	private static class UnmappedPolymorphismReplacer extends BaseSemanticQueryWalker {
		private final SqmRoot unmappedPolymorphicFromElement;
		private final SqmExpressableTypeEntity mappedDescriptor;

		private Map<SqmFrom,SqmFrom> sqmFromSqmCopyMap = new HashMap<>();
		private Map<SqmNavigableBinding, SqmNavigableBinding> navigableBindingCopyMap = new HashMap<>();

		private UnmappedPolymorphismReplacer(
				SqmSelectStatement selectStatement,
				SqmRoot unmappedPolymorphicFromElement,
				SqmExpressableTypeEntity mappedDescriptor) {
			this.unmappedPolymorphicFromElement = unmappedPolymorphicFromElement;
			this.mappedDescriptor = mappedDescriptor;
		}

		@Override
		public SqmStatement visitStatement(SqmStatement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmUpdateStatement visitUpdateStatement(SqmUpdateStatement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmSetClause visitSetClause(SqmSetClause setClause) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmAssignment visitAssignment(SqmAssignment assignment) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmDeleteStatement visitDeleteStatement(SqmDeleteStatement statement) {
			throw new UnsupportedOperationException( "Not valid" );
		}

		@Override
		public SqmSelectStatement visitSelectStatement(SqmSelectStatement statement) {
			final SqmSelectStatementImpl copy = new SqmSelectStatementImpl();
			copy.applyQuerySpec( visitQuerySpec( statement.getQuerySpec() ) );
			return copy;
		}

		@Override
		public SqmQuerySpec visitQuerySpec(SqmQuerySpec querySpec) {
			// NOTE : it is important that we visit the SqmFromClause first so that the
			// 		fromElementCopyMap gets built before other parts of the queryspec
			// 		are visited
			return new SqmQuerySpec(
					visitFromClause( querySpec.getFromClause() ),
					visitSelectClause( querySpec.getSelectClause() ),
					visitWhereClause( querySpec.getWhereClause() ),
					visitOrderByClause( querySpec.getOrderByClause() ),
					visitLimitOffsetClause( querySpec.getLimitOffsetClause() )
			);
		}

		private SqmFromClause currentFromClauseCopy = null;

		@Override
		public SqmFromClause visitFromClause(SqmFromClause fromClause) {
			final SqmFromClause previousCurrent = currentFromClauseCopy;

			try {
				SqmFromClause copy = new SqmFromClause();
				currentFromClauseCopy = copy;
				super.visitFromClause( fromClause );
				return copy;
			}
			finally {
				currentFromClauseCopy = previousCurrent;
			}
		}

		private FromElementSpace currentFromElementSpaceCopy;

		@Override
		public FromElementSpace visitFromElementSpace(FromElementSpace fromElementSpace) {
			if ( currentFromClauseCopy == null ) {
				throw new ParsingException( "Current SqmFromClause copy was null" );
			}

			final FromElementSpace previousCurrent = currentFromElementSpaceCopy;
			try {
				FromElementSpace copy = currentFromClauseCopy.makeFromElementSpace();
				currentFromElementSpaceCopy = copy;
				super.visitFromElementSpace( fromElementSpace );
				return copy;
			}
			finally {
				currentFromElementSpaceCopy = previousCurrent;
			}
		}

		// todo : it is really the bindings we want to keep track of..

		@Override
		public SqmRoot visitRootEntityFromElement(SqmRoot rootEntityFromElement) {
			final SqmNavigableSourceBinding existingCopy = (SqmNavigableSourceBinding) navigableBindingCopyMap.get( rootEntityFromElement.getBinding() );
			if ( existingCopy != null ) {
				return (SqmRoot) existingCopy.getExportedFromElement();
			}

			if ( currentFromElementSpaceCopy == null ) {
				throw new ParsingException( "Current FromElementSpace copy was null" );
			}
			if ( currentFromElementSpaceCopy.getRoot() != null ) {
				throw new ParsingException( "FromElementSpace copy already contains root." );
			}

			final SqmRoot copy;
			if ( rootEntityFromElement == unmappedPolymorphicFromElement ) {
				copy = new SqmRoot(
						currentFromElementSpaceCopy,
						rootEntityFromElement.getUniqueIdentifier(),
						rootEntityFromElement.getIdentificationVariable(),
						mappedDescriptor
				);
			}
			else {
				copy = new SqmRoot(
						currentFromElementSpaceCopy,
						rootEntityFromElement.getUniqueIdentifier(),
						rootEntityFromElement.getIdentificationVariable(),
						rootEntityFromElement.getBinding().getBoundNavigable()
				);
			}
			navigableBindingCopyMap.put( rootEntityFromElement.getBinding(), copy.getBinding() );
			return copy;
		}

		@Override
		public SqmCrossJoin visitCrossJoinedFromElement(SqmCrossJoin joinedFromElement) {
			final SqmNavigableSourceBinding existingCopy = (SqmNavigableSourceBinding) navigableBindingCopyMap.get( joinedFromElement.getBinding() );
			if ( existingCopy != null ) {
				return (SqmCrossJoin) existingCopy.getExportedFromElement();
			}

			if ( currentFromElementSpaceCopy == null ) {
				throw new ParsingException( "Current FromElementSpace copy was null" );
			}

			final SqmCrossJoin copy = new SqmCrossJoin(
					currentFromElementSpaceCopy,
					joinedFromElement.getUniqueIdentifier(),
					joinedFromElement.getIdentificationVariable(),
					joinedFromElement.getBinding().getBoundNavigable()
			);
			navigableBindingCopyMap.put( joinedFromElement.getBinding(), copy.getBinding() );
			return copy;
		}

		@Override
		public SqmEntityJoin visitQualifiedEntityJoinFromElement(SqmEntityJoin joinedFromElement) {
			final SqmNavigableSourceBinding existingCopy = (SqmNavigableSourceBinding) navigableBindingCopyMap.get( joinedFromElement.getBinding() );
			if ( existingCopy != null ) {
				return (SqmEntityJoin) existingCopy.getExportedFromElement();
			}

			if ( currentFromElementSpaceCopy == null ) {
				throw new ParsingException( "Current FromElementSpace copy was null" );
			}

			final SqmEntityJoin copy = new SqmEntityJoin(
					currentFromElementSpaceCopy,
					joinedFromElement.getUniqueIdentifier(),
					joinedFromElement.getIdentificationVariable(),
					joinedFromElement.getBinding().getBoundNavigable(),
					joinedFromElement.getJoinType()
			);
			navigableBindingCopyMap.put( joinedFromElement.getBinding(), copy.getBinding() );
			return copy;
		}

		@Override
		public SqmAttributeJoin visitQualifiedAttributeJoinFromElement(SqmAttributeJoin joinedFromElement) {
			final SqmSingularAttributeBinding existingCopy = (SqmSingularAttributeBinding) navigableBindingCopyMap.get( joinedFromElement.getBinding() );
			if ( existingCopy != null ) {
				return (SqmAttributeJoin) existingCopy.getExportedFromElement();
			}

			if ( currentFromElementSpaceCopy == null ) {
				throw new ParsingException( "Current FromElementSpace copy was null" );
			}

			if ( joinedFromElement.getAttributeBinding().getExportedFromElement() == null ) {
				throw new ParsingException( "Could not determine attribute join's LHS for copy" );
			}

			return makeCopy( joinedFromElement );
		}

		private SqmAttributeJoin makeCopy(SqmAttributeJoin fromElement) {
			assert fromElement.getAttributeBinding().getSourceBinding() != null;

			if ( fromElement == null ) {
				return null;
			}

			final SqmNavigableSourceBinding sourceBindingCopy = (SqmNavigableSourceBinding) navigableBindingCopyMap.get(
					fromElement.getAttributeBinding().getSourceBinding()
			);

			if ( sourceBindingCopy == null ) {
				throw new ParsingException( "Could not determine attribute join's LHS for copy" );
			}

			assert NavigableBindingHelper.resolveExportedFromElement( sourceBindingCopy ).getContainingSpace() == currentFromElementSpaceCopy;

			final SqmAttributeBinding attributeBindingCopy = (SqmAttributeBinding) NavigableBindingHelper.createNavigableBinding(
					sourceBindingCopy,
					fromElement.getAttributeBinding().getBoundNavigable()
			);

			final SqmAttributeJoin copy = new SqmAttributeJoin(
					sourceBindingCopy.getExportedFromElement(),
					attributeBindingCopy,
					fromElement.getUniqueIdentifier(),
					fromElement.getIdentificationVariable(),
					fromElement.getIntrinsicSubclassIndicator(),
					fromElement.getJoinType(),
					fromElement.isFetched()
			);
			navigableBindingCopyMap.put( fromElement.getAttributeBinding(), copy.getAttributeBinding() );
			return copy;
		}

		@Override
		public SqmSelectClause visitSelectClause(SqmSelectClause selectClause) {
			SqmSelectClause copy = new SqmSelectClause( selectClause.isDistinct() );
			for ( SqmSelection selection : selectClause.getSelections() ) {
				copy.addSelection( visitSelection( selection ) );
			}
			return copy;
		}

		@Override
		public SqmSelection visitSelection(SqmSelection selection) {
			return new SqmSelection(
					(SqmExpression) selection.getExpression().accept( this ),
					selection.getAlias()
			);
		}

		@Override
		public SqmDynamicInstantiation visitDynamicInstantiation(SqmDynamicInstantiation dynamicInstantiation) {
			SqmDynamicInstantiation copy = dynamicInstantiation.makeShallowCopy();
			for ( SqmDynamicInstantiationArgument aliasedArgument : dynamicInstantiation.getArguments() ) {
				copy.addArgument(
						new SqmDynamicInstantiationArgument(
								(SqmExpression) aliasedArgument.getExpression().accept( this ),
								aliasedArgument.getAlias()
						)
				);
			}
			return copy;
		}

		@Override
		public SqmWhereClause visitWhereClause(SqmWhereClause whereClause) {
			if ( whereClause == null ) {
				return null;
			}
			return new SqmWhereClause( (SqmPredicate) whereClause.getPredicate().accept( this ) );
		}

		@Override
		public GroupedSqmPredicate visitGroupedPredicate(GroupedSqmPredicate predicate) {
			return new GroupedSqmPredicate( (SqmPredicate) predicate.accept( this ) );
		}

		@Override
		public AndSqmPredicate visitAndPredicate(AndSqmPredicate predicate) {
			return new AndSqmPredicate(
					(SqmPredicate) predicate.getLeftHandPredicate().accept( this ),
					(SqmPredicate) predicate.getRightHandPredicate().accept( this )
			);
		}

		@Override
		public OrSqmPredicate visitOrPredicate(OrSqmPredicate predicate) {
			return new OrSqmPredicate(
					(SqmPredicate) predicate.getLeftHandPredicate().accept( this ),
					(SqmPredicate) predicate.getRightHandPredicate().accept( this )
			);
		}

		@Override
		public RelationalSqmPredicate visitRelationalPredicate(RelationalSqmPredicate predicate) {
			return new RelationalSqmPredicate(
					predicate.getOperator(),
					(SqmExpression) predicate.getLeftHandExpression().accept( this ),
					(SqmExpression) predicate.getRightHandExpression().accept( this )
			);
		}

		@Override
		public EmptinessSqmPredicate visitIsEmptyPredicate(EmptinessSqmPredicate predicate) {
			return new EmptinessSqmPredicate(
					(SqmPluralAttributeBinding) predicate.getExpression().accept( this ),
					predicate.isNegated()
			);
		}

		@Override
		public NullnessSqmPredicate visitIsNullPredicate(NullnessSqmPredicate predicate) {
			return new NullnessSqmPredicate(
					(SqmExpression) predicate.getExpression().accept( this ),
					predicate.isNegated()
			);
		}

		@Override
		public BetweenSqmPredicate visitBetweenPredicate(BetweenSqmPredicate predicate) {
			return new BetweenSqmPredicate(
					(SqmExpression) predicate.getExpression().accept( this ),
					(SqmExpression) predicate.getLowerBound().accept( this ),
					(SqmExpression) predicate.getUpperBound().accept( this ),
					predicate.isNegated()
			);
		}

		@Override
		public LikeSqmPredicate visitLikePredicate(LikeSqmPredicate predicate) {
			return new LikeSqmPredicate(
					(SqmExpression) predicate.getMatchExpression().accept( this ),
					(SqmExpression) predicate.getPattern().accept( this ),
					(SqmExpression) predicate.getEscapeCharacter().accept( this )
			);
		}

		@Override
		public MemberOfSqmPredicate visitMemberOfPredicate(MemberOfSqmPredicate predicate) {
			final SqmSingularAttributeBinding attributeBindingCopy = resolveAttributeBinding( predicate.getAttributeBinding() );

			return new MemberOfSqmPredicate( attributeBindingCopy );
		}

//		private DomainReferenceBinding resolveDomainReferenceBinding(DomainReferenceBinding binding) {
//			DomainReferenceBinding copy = navigableBindingCopyMap.get( binding );
//			if ( copy == null ) {
//				copy = makeDomainReferenceBindingCopy( binding );
//				navigableBindingCopyMap.put( binding, copy );
//			}
//			return copy;
//		}

//		private DomainReferenceBinding makeDomainReferenceBindingCopy(DomainReferenceBinding binding) {
//			if ( binding instanceof AttributeBinding ) {
//				final AttributeBinding attributeBinding = (AttributeBinding) binding;
//				return new AttributeBinding(
//						resolveDomainReferenceBinding( attributeBinding.getLhs() ),
//						attributeBinding.getBoundDomainReference(),
//						attributeBinding.getFromElement()
//				);
//			}
//			else if ( binding instanceof )
//		}

		private SqmSingularAttributeBinding resolveAttributeBinding(SqmSingularAttributeBinding attributeBinding) {
			// its an attribute join... there has to be a source
			assert attributeBinding.getSourceBinding() != null;

			SqmSingularAttributeBinding attributeBindingCopy = (SqmSingularAttributeBinding) navigableBindingCopyMap.get( attributeBinding );
			if ( attributeBindingCopy == null ) {
				attributeBindingCopy = makeCopy( attributeBinding );
			}

			return attributeBindingCopy;
		}

		private SqmSingularAttributeBinding makeCopy(SqmSingularAttributeBinding attributeBinding) {
			// its an attribute join... there has to be a source
			assert attributeBinding.getSourceBinding() != null;

			assert !navigableBindingCopyMap.containsKey( attributeBinding );

			final SqmAttributeJoin originalJoin = (SqmAttributeJoin) sqmFromSqmCopyMap.get( attributeBinding.getExportedFromElement() );
			final SqmNavigableSourceBinding sourceBindingCopy = (SqmNavigableSourceBinding) navigableBindingCopyMap.get(
					attributeBinding.getSourceBinding()
			);

			if ( sourceBindingCopy == null ) {
				throw new ParsingException( "Could not resolve NavigableSourceBinding copy during query splitting" );
			}

			final SqmSingularAttributeBinding attributeBindingCopy = NavigableBindingHelper.createSingularAttributeBinding(
					sourceBindingCopy,
					attributeBinding.getBoundNavigable()
			);
			navigableBindingCopyMap.put( attributeBinding, attributeBindingCopy );
			return attributeBindingCopy;
		}

		@Override
		public NegatedSqmPredicate visitNegatedPredicate(NegatedSqmPredicate predicate) {
			return new NegatedSqmPredicate(
					(SqmPredicate) predicate.getWrappedPredicate().accept( this )
			);
		}

		@Override
		public InListSqmPredicate visitInListPredicate(InListSqmPredicate predicate) {
			InListSqmPredicate copy = new InListSqmPredicate(
					(SqmExpression) predicate.getTestExpression().accept( this )
			);
			for ( SqmExpression expression : predicate.getListExpressions() ) {
				copy.addExpression( (SqmExpression) expression.accept( this ) );
			}
			return copy;
		}

		@Override
		public InSubQuerySqmPredicate visitInSubQueryPredicate(InSubQuerySqmPredicate predicate) {
			return new InSubQuerySqmPredicate(
					(SqmExpression) predicate.getTestExpression().accept( this ),
					visitSubQueryExpression( predicate.getSubQueryExpression() )
			);
		}

		@Override
		public OrderByClause visitOrderByClause(OrderByClause orderByClause) {
			if ( orderByClause == null ) {
				return null;
			}

			OrderByClause copy = new OrderByClause();
			for ( SortSpecification sortSpecification : orderByClause.getSortSpecifications() ) {
				copy.addSortSpecification( visitSortSpecification( sortSpecification ) );
			}
			return copy;
		}

		@Override
		public SortSpecification visitSortSpecification(SortSpecification sortSpecification) {
			return new SortSpecification(
					(SqmExpression) sortSpecification.getSortExpression().accept( this ),
					sortSpecification.getCollation(),
					sortSpecification.getSortOrder()
			);
		}

		@Override
		public LimitOffsetClause visitLimitOffsetClause(LimitOffsetClause limitOffsetClause) {
			if ( limitOffsetClause == null ) {
				return null;
			}

			return new LimitOffsetClause(
					(SqmExpression) limitOffsetClause.getLimitExpression().accept( this ),
					(SqmExpression) limitOffsetClause.getOffsetExpression().accept( this )
			);
		}

		@Override
		public PositionalParameterSqmExpression visitPositionalParameterExpression(PositionalParameterSqmExpression expression) {
			return new PositionalParameterSqmExpression( expression.getPosition(), expression.allowMultiValuedBinding() );
		}

		@Override
		public NamedParameterSqmExpression visitNamedParameterExpression(NamedParameterSqmExpression expression) {
			return new NamedParameterSqmExpression( expression.getName(), expression.allowMultiValuedBinding() );
		}

		@Override
		public EntityTypeLiteralSqmExpression visitEntityTypeLiteralExpression(EntityTypeLiteralSqmExpression expression) {
			return new EntityTypeLiteralSqmExpression( expression.getExpressionType() );
		}

		@Override
		public UnaryOperationSqmExpression visitUnaryOperationExpression(UnaryOperationSqmExpression expression) {
			return new UnaryOperationSqmExpression(
					expression.getOperation(),
					(SqmExpression) expression.getOperand().accept( this )
			);
		}

		@Override
		public Object visitAttributeReferenceExpression(SqmAttributeBinding attributeBinding) {
			assert attributeBinding.getSourceBinding() != null;

			SqmAttributeBinding attributeBindingCopy = (SqmAttributeBinding) navigableBindingCopyMap.get( attributeBinding );
			if ( attributeBindingCopy == null ) {
				attributeBindingCopy = (SqmAttributeBinding) NavigableBindingHelper.createNavigableBinding(
						attributeBinding.getSourceBinding(),
						attributeBinding.getBoundNavigable()
				);
				navigableBindingCopyMap.put( attributeBinding, attributeBindingCopy );
			}
			return attributeBindingCopy;
		}

		@Override
		public GenericFunctionSqmExpression visitGenericFunction(GenericFunctionSqmExpression expression) {
			List<SqmExpression> argumentsCopy = new ArrayList<>();
			for ( SqmExpression argument : expression.getArguments() ) {
				argumentsCopy.add( (SqmExpression) argument.accept( this ) );
			}
			return new GenericFunctionSqmExpression(
					expression.getFunctionName(),
					(SqmDomainTypeBasic) expression.getExpressionType().getExportedDomainType(),
					argumentsCopy
			);
		}

		@Override
		public AvgFunctionSqmExpression visitAvgFunction(AvgFunctionSqmExpression expression) {
			return new AvgFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					(SqmDomainTypeBasic) expression.getExpressionType().getExportedDomainType()
			);
		}

		@Override
		public CountStarFunctionSqmExpression visitCountStarFunction(CountStarFunctionSqmExpression expression) {
			return new CountStarFunctionSqmExpression(
					expression.isDistinct(),
					(SqmDomainTypeBasic) expression.getExpressionType().getExportedDomainType()
			);
		}

		@Override
		public CountFunctionSqmExpression visitCountFunction(CountFunctionSqmExpression expression) {
			return new CountFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					(SqmDomainTypeBasic) expression.getExpressionType().getExportedDomainType()
			);
		}

		@Override
		public MaxFunctionSqmExpression visitMaxFunction(MaxFunctionSqmExpression expression) {
			return new MaxFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					(SqmDomainTypeBasic) expression.getExpressionType().getExportedDomainType()
			);
		}

		@Override
		public MinFunctionSqmExpression visitMinFunction(MinFunctionSqmExpression expression) {
			return new MinFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					(SqmDomainTypeBasic) expression.getExpressionType().getExportedDomainType()
			);
		}

		@Override
		public SumFunctionSqmExpression visitSumFunction(SumFunctionSqmExpression expression) {
			return new SumFunctionSqmExpression(
					(SqmExpression) expression.getArgument().accept( this ),
					expression.isDistinct(),
					(SqmDomainTypeBasic) expression.getExpressionType().getExportedDomainType()
			);
		}

		@Override
		public LiteralStringSqmExpression visitLiteralStringExpression(LiteralStringSqmExpression expression) {
			return new LiteralStringSqmExpression( expression.getLiteralValue(), expression.getExpressionType() );
		}

		@Override
		public LiteralCharacterSqmExpression visitLiteralCharacterExpression(LiteralCharacterSqmExpression expression) {
			return new LiteralCharacterSqmExpression( expression.getLiteralValue(), expression.getExpressionType() );
		}

		@Override
		public LiteralDoubleSqmExpression visitLiteralDoubleExpression(LiteralDoubleSqmExpression expression) {
			return new LiteralDoubleSqmExpression( expression.getLiteralValue(), expression.getExpressionType() );
		}

		@Override
		public LiteralIntegerSqmExpression visitLiteralIntegerExpression(LiteralIntegerSqmExpression expression) {
			return new LiteralIntegerSqmExpression( expression.getLiteralValue(), expression.getExpressionType() );
		}

		@Override
		public LiteralBigIntegerSqmExpression visitLiteralBigIntegerExpression(LiteralBigIntegerSqmExpression expression) {
			return new LiteralBigIntegerSqmExpression( expression.getLiteralValue(), expression.getExpressionType() );
		}

		@Override
		public LiteralBigDecimalSqmExpression visitLiteralBigDecimalExpression(LiteralBigDecimalSqmExpression expression) {
			return new LiteralBigDecimalSqmExpression( expression.getLiteralValue(), expression.getExpressionType() );
		}

		@Override
		public LiteralFloatSqmExpression visitLiteralFloatExpression(LiteralFloatSqmExpression expression) {
			return new LiteralFloatSqmExpression( expression.getLiteralValue(), expression.getExpressionType() );
		}

		@Override
		public LiteralLongSqmExpression visitLiteralLongExpression(LiteralLongSqmExpression expression) {
			return new LiteralLongSqmExpression( expression.getLiteralValue(), expression.getExpressionType() );
		}

		@Override
		public LiteralTrueSqmExpression visitLiteralTrueExpression(LiteralTrueSqmExpression expression) {
			return new LiteralTrueSqmExpression( expression.getExpressionType() );
		}

		@Override
		public LiteralFalseSqmExpression visitLiteralFalseExpression(LiteralFalseSqmExpression expression) {
			return new LiteralFalseSqmExpression( expression.getExpressionType() );
		}

		@Override
		public LiteralNullSqmExpression visitLiteralNullExpression(LiteralNullSqmExpression expression) {
			return new LiteralNullSqmExpression();
		}

		@Override
		public ConcatSqmExpression visitConcatExpression(ConcatSqmExpression expression) {
			return new ConcatSqmExpression(
					(SqmExpression) expression.getLeftHandOperand().accept( this ),
					(SqmExpression) expression.getRightHandOperand().accept( this )
			);
		}

		@Override
		public ConcatFunctionSqmExpression visitConcatFunction(ConcatFunctionSqmExpression expression) {
			final List<SqmExpression> arguments = new ArrayList<>();
			for ( SqmExpression argument : expression.getExpressions() ) {
				arguments.add( (SqmExpression) argument.accept( this ) );
			}

			return new ConcatFunctionSqmExpression(
					(SqmDomainTypeBasic) expression.getFunctionResultType().getExportedDomainType(),
					arguments
			);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ConstantEnumSqmExpression visitConstantEnumExpression(ConstantEnumSqmExpression expression) {
			return new ConstantEnumSqmExpression( expression.getValue(), expression.getExpressionType() );
		}

		@Override
		@SuppressWarnings("unchecked")
		public ConstantFieldSqmExpression visitConstantFieldExpression(ConstantFieldSqmExpression expression) {
			return new ConstantFieldSqmExpression( expression.getSourceField(), expression.getValue(), expression.getExpressionType() );
		}

		@Override
		public BinaryArithmeticSqmExpression visitBinaryArithmeticExpression(BinaryArithmeticSqmExpression expression) {
			return new BinaryArithmeticSqmExpression(
					expression.getOperation(),
					(SqmExpression) expression.getLeftHandOperand().accept( this ),
					(SqmExpression) expression.getRightHandOperand().accept( this ),
					expression.getExpressionType()
			);
		}

		@Override
		public SubQuerySqmExpression visitSubQueryExpression(SubQuerySqmExpression expression) {
			return new SubQuerySqmExpression(
					visitQuerySpec( expression.getQuerySpec() ),
					// assume already validated
					expression.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression().getExpressionType()
			);
		}
	}

}
