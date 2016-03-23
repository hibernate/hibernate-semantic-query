/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal;

import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParserBaseListener;

import org.jboss.logging.Logger;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * @author Steve Ebersole
 */
public class HqlParseTreePrinter extends HqlParserBaseListener {
	private static final Logger HQL_LOGGER = Logger.getLogger( "org.hibernate.sqm.hql.parseTree" );

	public static void logParseTree(HqlParser parser) {
		if ( !HQL_LOGGER.isDebugEnabled() ) {
			return;
		}

		ParseTreeWalker.DEFAULT.walk( new HqlParseTreePrinter( parser ), parser.statement() );
		parser.reset();
	}

	private final HqlParser parser;

	private int depth = 0;

	public HqlParseTreePrinter(HqlParser parser) {
		this.parser = parser;
	}

	@Override
	public void enterEveryRule(@NotNull ParserRuleContext ctx) {
		final String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];

		if ( !ruleName.endsWith( "Keyword" ) ) {
			HQL_LOGGER.debugf(
					"%s %s (%s) [`%s`]",
					enterRulePadding(),
					ctx.getClass().getSimpleName(),
					ruleName,
					ctx.getText()
			);
		}
		super.enterEveryRule( ctx );
	}

	private String enterRulePadding() {
		return pad( depth++ ) + "->";
	}

	private String pad(int depth) {
		StringBuilder buf = new StringBuilder( 2 * depth );
		for ( int i = 0; i < depth; i++ ) {
			buf.append( "  " );
		}
		return buf.toString();
	}

	@Override
	public void exitEveryRule(@NotNull ParserRuleContext ctx) {
		super.exitEveryRule( ctx );

		final String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];

		if ( !ruleName.endsWith( "Keyword" ) ) {
			HQL_LOGGER.debugf(
					"%s %s (%s) [`%s`]",
					exitRulePadding(),
					ctx.getClass().getSimpleName(),
					parser.getRuleNames()[ctx.getRuleIndex()],
					ctx.getText()
			);
		}
	}

	private String exitRulePadding() {
		return pad( --depth ) + "<-";
	}
}
