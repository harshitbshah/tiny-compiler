package cop5556fa17;

import java.util.ArrayList;
import java.util.HashSet;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;
	
	HashSet<Kind> fDec;
	HashSet<Kind> fStmt; 
	
	HashSet<Kind> fVarDec;
	HashSet<Kind> fImgDec;
	HashSet<Kind> fSrcDec;
	
	HashSet<Kind> fUnaNot;
	
	HashSet<Kind> fPrimary;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		
		// Program
		fDec = new HashSet<Kind>();
		fDec.add(Kind.KW_int);
		fDec.add(Kind.KW_boolean);
		fDec.add(Kind.KW_image);
		fDec.add(Kind.KW_url);
		fDec.add(Kind.KW_file);

		fStmt = new HashSet<Kind>();
		fStmt.add(Kind.IDENTIFIER);
		
		// Declaration		
		fVarDec = new HashSet<Kind>();
		fVarDec.add(Kind.KW_int);
		fVarDec.add(Kind.KW_boolean);
		
		fImgDec = new HashSet<Kind>();
		fImgDec.add(Kind.KW_image);
		
		fSrcDec = new HashSet<Kind>();
		fSrcDec.add(Kind.KW_url);
		fSrcDec.add(Kind.KW_file);
 
		// UnaryExpressionNotPlusNotMinus
		fUnaNot = new HashSet<Kind>();
		fUnaNot.add(Kind.OP_EXCL);
		fUnaNot.add(Kind.INTEGER_LITERAL);
		fUnaNot.add(Kind.IDENTIFIER);
		fUnaNot.add(Kind.KW_x);
		fUnaNot.add(Kind.KW_y);
		fUnaNot.add(Kind.KW_r);
		fUnaNot.add(Kind.KW_a);
		fUnaNot.add(Kind.KW_X);
		fUnaNot.add(Kind.KW_Y);
		fUnaNot.add(Kind.KW_Z);
		fUnaNot.add(Kind.KW_A);
		fUnaNot.add(Kind.KW_R);
		fUnaNot.add(Kind.KW_DEF_X);
		fUnaNot.add(Kind.KW_DEF_Y);
		
		// Primary
		fPrimary = new HashSet<Kind>();
		fPrimary.add(Kind.INTEGER_LITERAL);
		fPrimary.add(Kind.BOOLEAN_LITERAL);
		fPrimary.add(Kind.LPAREN);
		fPrimary.add(Kind.KW_sin);
		fPrimary.add(Kind.KW_cos);
		fPrimary.add(Kind.KW_atan);
		fPrimary.add(Kind.KW_abs);
		fPrimary.add(Kind.KW_cart_x);
		fPrimary.add(Kind.KW_cart_y);
		fPrimary.add(Kind.KW_polar_a);
		fPrimary.add(Kind.KW_polar_r);
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		Token firstToken = t;
		Token name = t;
		
		match(Kind.IDENTIFIER);
		while(fDec.contains(t.kind) || fStmt.contains(t.kind)) {
			if (fDec.contains(t.kind)) {
				Declaration dec = null;
				if(fVarDec.contains(t.kind) || fSrcDec.contains(t.kind) || fImgDec.contains(t.kind)){
					if(fVarDec.contains(t.kind)) {
						dec = varDec();
					}
					else if(fSrcDec.contains(t.kind)) {
						dec = srcDec();
					}
					else {
						dec = imgDec();
					}
				}
				decsAndStatements.add(dec);
				match(Kind.SEMI);
			}
			else if(fStmt.contains(t.kind)) {
				Statement s = stmt();
				decsAndStatements.add(s);
				match(Kind.SEMI);
			}
			else
				throw new SyntaxException(t, "Unexpected Token");
		}
		
		Program p = new Program(firstToken, name, decsAndStatements);
		return p;
	}
	
	/*
	 * 	VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression | EP )
	 */
	Declaration varDec() throws SyntaxException {
		
		Token firstToken = t;
		Token type = t;
		Expression e = null;
		
		if(t.kind.equals(Kind.KW_int))
			match(Kind.KW_int);
		else if(t.kind.equals(Kind.KW_boolean))
			match(Kind.KW_boolean);
		else 
			throw new SyntaxException(t, "Unexpected Token");
		
		Token ident = t;
		match(Kind.IDENTIFIER);
		if(t.kind.equals(Kind.OP_ASSIGN)) {
			match(Kind.OP_ASSIGN);
			e = expression();
		}
		
		Declaration d = new Declaration_Variable(firstToken, type, ident, e);
		return d;
	}
		
	/*
	 * ImageDeclaration ::=  KW_image  (LSQUARE Expression COMMA Expression RSQUARE | EP) IDENTIFIER ( OP_LARROW Source |  EP)   
	 */
	Declaration imgDec() throws SyntaxException {
		Token firstToken = t;
		Expression xSize = null, ySize = null;
		match(Kind.KW_image);
		
		if(t.kind.equals(Kind.LSQUARE)) {
			match(Kind.LSQUARE);
			xSize = expression();
			match(Kind.COMMA);
			ySize = expression();
			match(Kind.RSQUARE);
		}
		
		Token name = t;
		match(Kind.IDENTIFIER);
		
		Source source = null;
		if(t.kind.equals(Kind.OP_LARROW)) {
			match(Kind.OP_LARROW);
			source = source();
		}
		
		return new Declaration_Image(firstToken, xSize, ySize, name, source);
	}
	
	/*
	 *  Source ::= STRING_LITERAL  | OP_AT Expression| IDENTIFIER
	 */
	Source source() throws SyntaxException {
		Token firstToken = t;
		
		Source s = null;
		if(t.kind.equals(Kind.STRING_LITERAL)) {
			Token fileOrUrl = t;
			s = new Source_StringLiteral(firstToken, fileOrUrl.getText());
			match(Kind.STRING_LITERAL);
		}
		else if(t.kind.equals(Kind.OP_AT)) {
			match(Kind.OP_AT);
			Expression paramNum = expression();
			s = new Source_CommandLineParam(firstToken, paramNum);
		} 
		else if(t.kind.equals(Kind.IDENTIFIER)) {
			Token name = t;
			s = new Source_Ident(firstToken, name);
			match(Kind.IDENTIFIER);
		}
		else {
			throw new SyntaxException(t, "Unexpected Token");
		}
		
		return s;
	}
	
	/*
	 *  SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source
	 */
	Declaration srcDec() throws SyntaxException {
		Token firstToken = t;
		Token type = t;
		if(t.kind.equals(Kind.KW_url)) {
			match(Kind.KW_url);
		}
		else if(t.kind.equals(Kind.KW_file)) {
			match(Kind.KW_file);
		} 
		else 
			throw new SyntaxException(t, "Unexpected Token");
		
		Token name = t;
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		Source source = source();
		
		return new Declaration_SourceSink(firstToken, type, name, source);
	}
	
	/*
	 * Statement ::= AssignmentStatement | ImageOutStatement | ImageInStatement
	 */
	Statement stmt() throws SyntaxException {
		
		Token iden = t;
		Token firstToken = t;
		Statement stmt = null;
		
		match(Kind.IDENTIFIER);
		
		if(t.kind.equals(Kind.LSQUARE) || t.kind.equals(Kind.OP_ASSIGN)) {
			Index index = null;
			if(t.kind.equals(Kind.LSQUARE)) {
				consume();
				index = lhsSel();
				match(Kind.RSQUARE);
			}
			match(Kind.OP_ASSIGN);
			Expression e = expression();
			
			stmt = new Statement_Assign(firstToken, new LHS(firstToken, iden, index), e);
		}
		else if (t.kind.equals(Kind.OP_RARROW)) {
			match(Kind.OP_RARROW);
			Sink sink = null;
			Token first = t;
			if(t.kind.equals(Kind.IDENTIFIER)) {
				sink = new Sink_Ident(first, first);
				match(Kind.IDENTIFIER);
			}
			else if(t.kind.equals(Kind.KW_SCREEN)) {
				sink = new Sink_SCREEN(first);
				match(Kind.KW_SCREEN);
			}
			else
				throw new SyntaxException(t, "Unexpected Token");
			
			stmt = new Statement_Out(firstToken, iden, sink);
		}
		else if(t.kind.equals(Kind.OP_LARROW)) {
			match(Kind.OP_LARROW);
			Source s = source();
			
			stmt = new Statement_In(firstToken, iden, s);
		}
		else
			throw new SyntaxException(t, "Unexpected Token");
		
		return stmt;
	}
	
	Index lhsSel() throws SyntaxException {
		Token firstToken1 = null, firstToken2 = null;
		match(Kind.LSQUARE);
		
		if(t.kind.equals(Kind.KW_x) || t.kind.equals(Kind.KW_r)) {
			if(t.kind.equals(Kind.KW_x)) {
				firstToken1 = t;
				consume();
				match(Kind.COMMA);
				
				firstToken2 = t;
				match(Kind.KW_y);
			}
			else {
				firstToken1 = t;
				consume();
				match(Kind.COMMA);
				
				firstToken2 = t;
				match(Kind.KW_a);
			}
		}
		match(Kind.RSQUARE);
		
		return new Index(firstToken1, new Expression_PredefinedName(firstToken1, firstToken1.kind), new Expression_PredefinedName(firstToken2, firstToken2.kind));
	}
	

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		//TODO implement this.
		Expression e = orExpr();
		if(t.kind.equals(Kind.OP_Q)) {
			Token firstToken = t;
			match(Kind.OP_Q);
			Expression tru = expression();
			match(Kind.OP_COLON);
			Expression fal = expression();
			
			return new Expression_Conditional(firstToken, e, tru, fal);
		}
		
		return e;		
	}
	
	Expression orExpr() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = andExpr();
		while(t.kind.equals(Kind.OP_OR)) {
			Token op = t;
			match(Kind.OP_OR);
			Expression e1 = andExpr();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}
	
	Expression andExpr() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = eqExpr();
		while(t.kind.equals(Kind.OP_AND)) {
			Token op = t;
			match(Kind.OP_AND);
			Expression e1 = eqExpr();
			e0 = new Expression_Binary(firstToken, e0, op, e1); 
		}
		
		return e0;
	}
	
	Expression eqExpr() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = relExpr();
		while(t.kind.equals(Kind.OP_EQ) || t.kind.equals(Kind.OP_NEQ)) {
			Token op = t;
			if(t.kind.equals(Kind.OP_EQ))
				match(Kind.OP_EQ);
			else if(t.kind.equals(Kind.OP_NEQ))
				match(Kind.OP_NEQ);
			else
				throw new SyntaxException(t, "Unexpected Token");
			Expression e1 = relExpr();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}
	
	Expression relExpr() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = addExpr();
		while(t.kind.equals(Kind.OP_LT) || t.kind.equals(Kind.OP_GT) || t.kind.equals(Kind.OP_LE) || t.kind.equals(Kind.OP_GE)) {
			Token op = t;
			if(t.kind.equals(Kind.OP_LT))
				match(Kind.OP_LT);
			else if(t.kind.equals(Kind.OP_GT))
				match(Kind.OP_GT);
			else if(t.kind.equals(Kind.OP_LE))
				match(Kind.OP_LE);
			else if(t.kind.equals(Kind.OP_GE))
				match(Kind.OP_GE);
			else
				throw new SyntaxException(t, "Unexpected Token");
			Expression e1 = addExpr();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}
	
	Expression addExpr() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = mulExpr();
		while(t.kind.equals(Kind.OP_PLUS) || t.kind.equals(Kind.OP_MINUS)) {
			Token op = t;
			if(t.kind.equals(Kind.OP_PLUS))
				match(Kind.OP_PLUS);
			else if(t.kind.equals(Kind.OP_MINUS))
				match(Kind.OP_MINUS);
			else
				throw new SyntaxException(t, "Unexpected Token");
			Expression e1 = mulExpr();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}
	
	Expression mulExpr() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = unExpr();
		while(t.kind.equals(Kind.OP_TIMES) || t.kind.equals(Kind.OP_DIV) || t.kind.equals(Kind.OP_MOD)) {
			Token op = t;
			if(t.kind.equals(Kind.OP_TIMES))
				match(Kind.OP_TIMES);
			else if(t.kind.equals(Kind.OP_DIV))
				match(Kind.OP_DIV);
			else if(t.kind.equals(Kind.OP_MOD))
				match(Kind.OP_MOD);
			else
				throw new SyntaxException(t, "Unexpected Token");
			Expression e1 = unExpr();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		
		return e0;
	}
	
	Expression unExpr() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		
		if(t.kind.equals(Kind.OP_PLUS) || t.kind.equals(Kind.OP_MINUS)) {
			Token op = t;
			if(t.kind.equals(Kind.OP_PLUS))
				match(Kind.OP_PLUS);
			else if(t.kind.equals(Kind.OP_MINUS))
				match(Kind.OP_MINUS);
			Expression e0 = unExpr();
			
			e = new Expression_Unary(firstToken, op, e0);
		}
		else if(fUnaNot.contains(t.kind) || fPrimary.contains(t.kind)) {
			e = unExprPlMin();
		}
		else 
			throw new SyntaxException(t, "Unexpected Token");

		return e;
	}
	
	Expression unExprPlMin() throws SyntaxException {
		
		Expression e = null;
		if(t.kind.equals(Kind.OP_EXCL)) {
			e = opExcl();
		}
		else if(fPrimary.contains(t.kind)) {
			e = primary();
		}
		else if(t.kind.equals(Kind.IDENTIFIER)){
			e = identOrPxl();
		}
		else
			e = expPre();
		
		return e;
	}
	
	Expression opExcl() throws SyntaxException {
		Token firstToken = t;
		Token op = t;
		
		match(Kind.OP_EXCL);
		Expression e0 = unExpr();
		
		return new Expression_Unary(firstToken, op, e0);
	}
	
	Expression identOrPxl() throws SyntaxException {
		
		Token firstToken = t;
		Token ident = t;
		consume();
		if(t.kind.equals(Kind.LSQUARE)) {
			consume();
			Index index = selector();
			match(Kind.RSQUARE);
			
			return new Expression_PixelSelector(firstToken, ident, index);
		}
		
		return new Expression_Ident(firstToken, ident);
	}
	
	Expression expPre() throws SyntaxException {
		Token firstToken = t;
		Kind k = t.kind;
		consume();
		return new Expression_PredefinedName(firstToken, k);
	}
	
	Expression primary() throws SyntaxException {
		
		if(t.kind.equals(Kind.INTEGER_LITERAL)) {
			int val = t.intVal();
			Token firstToken = t;
			consume();
			return new Expression_IntLit(firstToken, val);
		}
		else if(t.kind.equals(Kind.BOOLEAN_LITERAL)) {
			boolean val = Boolean.parseBoolean(t.getText());
			Token firstToken = t;
			consume();
			return new Expression_BooleanLit(firstToken, val);
		}
		else if(t.kind.equals(Kind.LPAREN)) {
			consume();
			Expression e = expression();
			match(Kind.RPAREN);
			return e;
		}
		else {
			return functionApp();
		}
	}
	
	Expression functionApp() throws SyntaxException {
		Token firstToken = t;
		Expression e;
		consume();
		if(t.kind.equals(Kind.LPAREN)) {
			consume();
			Expression arg = expression();
			match(Kind.RPAREN);
			e = new Expression_FunctionAppWithExprArg(firstToken, firstToken.kind, arg);
		}
		else if(t.kind.equals(Kind.LSQUARE)) {
			consume();
			Index arg = selector();
			match(Kind.RSQUARE);
			e = new Expression_FunctionAppWithIndexArg(firstToken, firstToken.kind, arg);
		}
		else
			throw new SyntaxException(t, "Unexpected Token");
		
		return e;
	}
	
	Index selector() throws SyntaxException {
		Token firstToken = t;
		
		Expression e0 = expression();
		match(Kind.COMMA);
		Expression e1 = expression();
		
		return new Index(firstToken, e0, e1);
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
	
	
	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		// if (t.isKind(kind)) {
		if (t.kind.equals(kind)) {
			return consume();
		}
		throw new SyntaxException(t, "saw " + t.kind + " expected " + kind);
	}
	
	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}
}
