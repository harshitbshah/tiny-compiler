package cop5556fa17;

import java.net.MalformedURLException;
import java.net.URL;

import cop5556fa17.Scanner.*;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
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
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		
		SymbolTable symtab = new SymbolTable();
	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}
	
	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		declaration_Image.setType(Type.IMAGE);
		
		if(declaration_Image.xSize != null)
			declaration_Image.xSize.visit(this, arg);
		
		if(declaration_Image.ySize != null)
			declaration_Image.ySize.visit(this, arg);
		
		if(declaration_Image.source != null)
			declaration_Image.source.visit(this, arg);
		
		boolean flag = symtab.insert(declaration_Image.name, declaration_Image);
		
		if(declaration_Image.xSize != null) {
			boolean compare = declaration_Image.ySize != null && declaration_Image.xSize.getType() == Type.INTEGER && declaration_Image.ySize.getType() == Type.INTEGER;
			if(!compare)
				throw new SemanticException(declaration_Image.firstToken, "exception in visitDeclaration_Image");
		}
		
		
		if (!flag) {
			throw new SemanticException(declaration_Image.firstToken, "exception in visitDeclaration_Image");
		}
		
		return declaration_Image;		
	}
	
	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		declaration_SourceSink.setType(TypeUtils.getType(declaration_SourceSink.firstToken));
			
		declaration_SourceSink.source.visit(this, null);
		
		boolean flag = symtab.insert(declaration_SourceSink.name, declaration_SourceSink);
		
		boolean compare = (declaration_SourceSink.getType() == declaration_SourceSink.source.getType()) || (declaration_SourceSink.source.getType() == null);	
		if (!flag || !compare) {
			throw new SemanticException(declaration_SourceSink.firstToken, "exception in visitDeclaration_SourceSink");
		}
			
		return declaration_SourceSink;
	}
	
	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		declaration_Variable.setType(TypeUtils.getType(declaration_Variable.firstToken));
		
		if(declaration_Variable.e != null){
			declaration_Variable.e.visit(this, null);
			if(declaration_Variable.getType() != declaration_Variable.e.getType())
				throw new SemanticException(declaration_Variable.firstToken, "exception in visitDeclaration_Variable");
		}
		boolean flag = symtab.insert(declaration_Variable.name, declaration_Variable);	
		
		if (!flag) {
			throw new SemanticException(declaration_Variable.firstToken, "exception in visitDeclaration_Variable");
		}
			
		return declaration_Variable;
	}
	
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Declaration name = symtab.lookup(statement_Out.name);
		statement_Out.setDec(name);
		
		statement_Out.sink.visit(this, null);
		
		boolean compare = ((name.getType() == Type.INTEGER || name.getType() == Type.BOOLEAN) && statement_Out.sink.getType() == Type.SCREEN)
				|| (name.getType() == Type.IMAGE && (statement_Out.sink.getType() == Type.FILE || statement_Out.sink.getType() == Type.SCREEN));
		
		if(!compare || name == null) {
			throw new SemanticException(statement_Out.firstToken, "exception in visitStatement_Out");
		}
			
		return statement_Out;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Declaration name = symtab.lookup(statement_In.name);
		statement_In.setDec(name);
		statement_In.source.visit(this, null);
		
		boolean compare = name.getType() == statement_In.source.getType();
				
//		if(!compare || name == null) {
//			throw new SemanticException(statement_In.firstToken, "exception in visitStatement_Out");
//		}
			
		return statement_In;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		statement_Assign.lhs.visit(this, null);
		statement_Assign.e.visit(this, null);
		
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		
		boolean compare = (statement_Assign.lhs.getType() == statement_Assign.e.getType()) || (statement_Assign.lhs.getType() == Type.IMAGE && statement_Assign.e.getType() == Type.INTEGER);	
		
		if (!compare) {
			throw new SemanticException(statement_Assign.firstToken, "exception in visitStatement_Assign");
		}
		
		return statement_Assign;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_Binary.e0.visit(this, null);
		expression_Binary.e1.visit(this, null);
		
		Type type;
		if(expression_Binary.op == Kind.OP_EQ || expression_Binary.op == Kind.OP_NEQ)
			type = Type.BOOLEAN;
		else if((expression_Binary.op == Kind.OP_GE || expression_Binary.op == Kind.OP_GT || expression_Binary.op == Kind.OP_LT || expression_Binary.op == Kind.OP_LE) && expression_Binary.e0.getType() == Type.INTEGER)
			type = Type.BOOLEAN;
		else if((expression_Binary.op == Kind.OP_AND || expression_Binary.op == Kind.OP_OR) && (expression_Binary.e0.getType() == Type.INTEGER || expression_Binary.e0.getType() == Type.BOOLEAN))
			type = expression_Binary.e0.getType();
		else if((expression_Binary.op == Kind.OP_DIV || expression_Binary.op == Kind.OP_MINUS || expression_Binary.op == Kind.OP_MOD || expression_Binary.op == Kind.OP_PLUS || expression_Binary.op == Kind.OP_POWER || expression_Binary.op == Kind.OP_TIMES) && expression_Binary.e0.getType() == Type.INTEGER)
			type = Type.INTEGER;
		else
			type = null;
			
		expression_Binary.setType(type);
		
		boolean compare = ((expression_Binary.e0.getType() == expression_Binary.e1.getType()) && expression_Binary.getType() != null);	
		
		if (!compare) {
			throw new SemanticException(expression_Binary.firstToken, "exception in visitExpression_Conditional");
		}
		
		return expression_Binary;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		expression_Unary.e.visit(this, null);
		
		Type type;
		if(expression_Unary.op == Kind.OP_EXCL && (expression_Unary.e.getType() == Type.BOOLEAN || expression_Unary.e.getType() == Type.INTEGER))
			type = expression_Unary.e.getType();
		else if((expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS) && expression_Unary.e.getType() == Type.INTEGER)
			type = Type.INTEGER;
		else
			type = null;
			
		expression_Unary.setType(type);
		
		boolean compare = expression_Unary.getType() != null;	
		
		if (!compare) {
			throw new SemanticException(expression_Unary.firstToken, "exception in visitExpression_Conditional");
		}
		
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		index.e0.visit(this, null);
		index.e1.visit(this, null);
		
		boolean compare = (index.e0.getType() == Type.INTEGER) && (index.e1.getType() == Type.INTEGER);
		if (!compare) {
			throw new SemanticException(index.firstToken, "exception in visitIndex");
		}
		
		index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_a));
		
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Declaration name = symtab.lookup(expression_PixelSelector.name);
		expression_PixelSelector.index.visit(this, null);
		
		Type type;
		if(name.getType() == Type.IMAGE)
			type = Type.INTEGER;
		else if(expression_PixelSelector.index == null)
			type = name.getType();
		else
			type = null;
			
		expression_PixelSelector.setType(type);
		
		boolean compare = expression_PixelSelector.getType() != null;	
		
		if (!compare) {
			throw new SemanticException(expression_PixelSelector.firstToken, "exception in visitExpression_Conditional");
		}
		
		return expression_PixelSelector;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub	
		expression_Conditional.condition.visit(this, null);
		expression_Conditional.trueExpression.visit(this, null);
		expression_Conditional.falseExpression.visit(this, null);
		
		boolean compare = (expression_Conditional.condition.getType() == Type.BOOLEAN) && (expression_Conditional.trueExpression.getType() == expression_Conditional.falseExpression.getType());	
		
		if (!compare) {
			throw new SemanticException(expression_Conditional.firstToken, "exception in visitExpression_Conditional");
		}
		
		expression_Conditional.setType(expression_Conditional.trueExpression.getType());
		
		return expression_Conditional;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
	    URL u = null;
	    Type type;
	    try {  
	        u = new URL(source_StringLiteral.fileOrUrl);
	        type = Type.URL;
	    } catch (MalformedURLException e) {  
	        type = Type.FILE;
	    }
		
		source_StringLiteral.setType(type);
		
		return source_StringLiteral;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		source_CommandLineParam.paramNum.visit(this, null);
//		source_CommandLineParam.setType(source_CommandLineParam.paramNum.getType());
		source_CommandLineParam.setType(null);
		
//		boolean compare = source_CommandLineParam.getType() == Type.INTEGER;
		boolean compare = source_CommandLineParam.paramNum.getType() == Type.INTEGER;
		
		if (!compare) {
			throw new SemanticException(source_CommandLineParam.firstToken, "exception in visitExpression_Conditional");
		}
		
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub	
		if(symtab.lookup(source_Ident.name) != null)
			source_Ident.setType(symtab.lookup(source_Ident.name).getType());
		
		boolean compare = (source_Ident.getType() == Type.FILE) || (source_Ident.getType() == Type.URL);
		
		if (!compare) {
			throw new SemanticException(source_Ident.firstToken, "exception in visitExpression_Conditional");
		}
		
		return source_Ident;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_IntLit.setType(Type.INTEGER);
		
		return expression_IntLit;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_FunctionAppWithExprArg.setType(Type.INTEGER);
		
		expression_FunctionAppWithExprArg.arg.visit(this, null);
		boolean compare = (expression_FunctionAppWithExprArg.arg.getType() == Type.INTEGER);	
		
		if (!compare) {
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "exception in visitExpression_FunctionAppWithExprArg");
		}
		
		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_FunctionAppWithIndexArg.setType(Type.INTEGER);
		
		expression_FunctionAppWithIndexArg.arg.visit(this, null);
		
		return expression_FunctionAppWithIndexArg;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.setType(Type.INTEGER);
		
		return expression_PredefinedName;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(lhs.index != null)
			lhs.index.visit(this, null);
		lhs.declaration = symtab.lookup(lhs.name);
		if(lhs.declaration != null)
			lhs.setType(lhs.declaration.getType());
		if(lhs.index != null)
			lhs.setCartesian(lhs.index.isCartesian());
		
		return lhs;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		sink_SCREEN.setType(Type.SCREEN);
		
		return sink_SCREEN;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(symtab.lookup(sink_Ident.name) != null)
			sink_Ident.setType(symtab.lookup(sink_Ident.name).getType());
		
		boolean compare = (sink_Ident.getType() == Type.FILE);	
		
		if (!compare) {
			throw new SemanticException(sink_Ident.firstToken, "exception in visitDeclaration_Variable");
		}
		
		return sink_Ident;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		expression_BooleanLit.setType(Type.BOOLEAN);
		
		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(symtab.lookup(expression_Ident.name) != null)
			expression_Ident.setType(symtab.lookup(expression_Ident.name).getType());
		
		return expression_Ident;
	}

}
