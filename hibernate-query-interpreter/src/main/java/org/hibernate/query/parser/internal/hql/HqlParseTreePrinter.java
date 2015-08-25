/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql;

import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.antlr.HqlParserBaseListener;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * @author Steve Ebersole
 */
public class HqlParseTreePrinter extends HqlParserBaseListener {
	private final HqlParser parser;

	private int depth = 0;

	public HqlParseTreePrinter(HqlParser parser) {
		this.parser = parser;
	}

	@Override
	public void enterEveryRule(@NotNull ParserRuleContext ctx) {
		final String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];

		if ( !ruleName.endsWith( "Keyword" ) ) {
			System.out.println(
					String.format(
							"%s %s (%s) [`%s`]",
							enterRulePadding(),
							ctx.getClass().getSimpleName(),
							ruleName,
							ctx.getText()
					)
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
			System.out.println(
					String.format(
							"%s %s (%s) [`%s`]",
							exitRulePadding(),
							ctx.getClass().getSimpleName(),
							parser.getRuleNames()[ctx.getRuleIndex()],
							ctx.getText()
					)
			);
		}
	}

	private String exitRulePadding() {
		return pad( --depth ) + "<-";
	}
}
