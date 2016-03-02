/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql;

import org.hibernate.sqm.parser.internal.hql.antlr.HqlLexer;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;

import org.jboss.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * @author Steve Ebersole
 */
public class HqlParseTreeBuilder {
	private static final Logger log = Logger.getLogger( HqlParseTreeBuilder.class );

	/**
	 * Singleton access
	 */
	public static final HqlParseTreeBuilder INSTANCE = new HqlParseTreeBuilder();

	public HqlParser parseHql(String hql) {
		// Build the lexer
		HqlLexer hqlLexer = new HqlLexer( new ANTLRInputStream( hql ) );

		// Build the parser...
		final HqlParser parser = new HqlParser( new CommonTokenStream( hqlLexer ) ) {
			@Override
			protected void logUseOfReservedWordAsIdentifier(Token token) {
				log.debugf( "Encountered use of reserved word as identifier : " + token.getText() );
			}
		};

		HqlParseTreePrinter.logParseTree( parser );

		return parser;
	}
}
