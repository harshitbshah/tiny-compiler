package cop5556fa17;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.ImageFrame;
import cop5556fa17.ImageSupport;
import cop5556fa17.Scanner.Kind;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 4);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 5);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 6);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 7);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 8);
		
//		mv.visitLocalVariable("DEF_X", "I", null, mainStart, mainEnd, 9);
//		mv.visitLdcInsn(new Integer(256));
//		mv.visitVarInsn(ISTORE, 9);
		
//		mv.visitLocalVariable("DEF_Y", "I", null, mainStart, mainEnd, 10);
//		mv.visitLdcInsn(new Integer(256));
//		mv.visitVarInsn(ISTORE, 10);
		
//		mv.visitLocalVariable("Z", "I", null, mainStart, mainEnd, 11);
//		mv.visitLdcInsn(new Integer(16777215));
//		mv.visitVarInsn(ISTORE, 11);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0,0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		FieldVisitor fv;
		if(declaration_Variable.getType() == Type.INTEGER)
			 fv = cw.visitField(ACC_STATIC, declaration_Variable.name, "I", null, new Integer(0));
		else
			 fv = cw.visitField(ACC_STATIC, declaration_Variable.name, "Z", null, new Boolean(false));
		
		fv.visitEnd();
		
		if(declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);
			if(declaration_Variable.getType() == Type.INTEGER)
				mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "I");
			else
				mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "Z");
		}

//		throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		
		Type typeE0 = expression_Binary.e0.getType();
		Type typeE1 = expression_Binary.e1.getType();
		
		if (expression_Binary.op == Kind.OP_PLUS)
			mv.visitInsn(IADD);
		else if(expression_Binary.op == Kind.OP_MINUS)
			mv.visitInsn(ISUB);
		else if(expression_Binary.op == Kind.OP_MOD)
			mv.visitInsn(IREM);
		else if(expression_Binary.op == Kind.OP_TIMES)
			mv.visitInsn(IMUL);
		else if(expression_Binary.op == Kind.OP_DIV)
			mv.visitInsn(IDIV);
		else if(expression_Binary.op == Kind.OP_AND)
			mv.visitInsn(IAND);
		else if(expression_Binary.op == Kind.OP_OR)
			mv.visitInsn(IOR);
		else if (expression_Binary.op == Kind.OP_LE) {
			Label labelStart = new Label();
			Label labelEnd = new Label();
			mv.visitJumpInsn(IF_ICMPLE, labelStart);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);
		}
	    else if (expression_Binary.op == Kind.OP_LT) {
	
			Label labelStart = new Label();
			Label labelEnd = new Label();
			mv.visitJumpInsn(IF_ICMPLT, labelStart);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);
	
		}
	    else if (expression_Binary.op == Kind.OP_GT) {

			Label labelStart = new Label();
			Label labelEnd = new Label();
			mv.visitJumpInsn(IF_ICMPGT, labelStart);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);

		} else if (expression_Binary.op == Kind.OP_GE) {

			Label labelStart = new Label();
			Label labelEnd = new Label();
			mv.visitJumpInsn(IF_ICMPGE, labelStart);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);

		} else if (expression_Binary.op == Kind.OP_EQ) {
			if ((typeE0.equals(Type.INTEGER) && typeE1.equals(Type.INTEGER))
					|| (typeE0.equals(Type.BOOLEAN) && typeE1.equals(Type.BOOLEAN))) {

				Label labelStart = new Label();
				Label labelEnd = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, labelStart);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, labelEnd);
				mv.visitLabel(labelStart);
				mv.visitInsn(ICONST_1);

				mv.visitLabel(labelEnd);
			} else if (typeE0.equals(typeE1)) {

				Label labelStart = new Label();
				Label labelEnd = new Label();
				mv.visitJumpInsn(IF_ACMPEQ, labelStart);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, labelEnd);
				mv.visitLabel(labelStart);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelEnd);
			}

		} else if (expression_Binary.op == Kind.OP_NEQ) {
			if ((typeE0.equals(Type.INTEGER) && typeE1.equals(Type.INTEGER))
					|| (typeE0.equals(Type.BOOLEAN) && typeE1.equals(Type.BOOLEAN))) {

				Label labelStart = new Label();
				Label labelEnd = new Label();
				mv.visitJumpInsn(IF_ICMPNE, labelStart);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, labelEnd);
				mv.visitLabel(labelStart);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelEnd);

			} else if (typeE0.equals(typeE1)) {

				Label labelStart = new Label();
				Label labelEnd = new Label();
				mv.visitJumpInsn(IF_ACMPNE, labelStart);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, labelEnd);
				mv.visitLabel(labelStart);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelEnd);
			}
		}
			
		
//		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getType());
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		
		expression_Unary.e.visit(this, arg);
		
		if(expression_Unary.op == Kind.OP_PLUS) {
			
		}
		else if(expression_Unary.op == Kind.OP_MINUS) {
			mv.visitInsn(INEG);
		}
		else if(expression_Unary.op == Kind.OP_EXCL) {
			if(expression_Unary.getType() == Type.INTEGER) {
				mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
				mv.visitInsn(IXOR);
			}
			else if(expression_Unary.getType() == Type.BOOLEAN) {
				Label labelStart = new Label();
				Label labelEnd = new Label();
				mv.visitJumpInsn(IFEQ, labelStart);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, labelEnd);
				mv.visitLabel(labelStart);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelEnd);
			}
		}
		
//		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		
		if(index.isCartesian() == false) {
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_x", "(II)I", false);	
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_y", "(II)I", false);	
		}
		
//		throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, "Ljava/awt/image/BufferedImage;");
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getPixel", "(Ljava/awt/image/BufferedImage;II)I", false);
		
//		throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		expression_Conditional.condition.visit(this, arg);
		
		Label labelStart = new Label();
		Label labelEnd = new Label();
		mv.visitJumpInsn(IFEQ, labelStart);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, labelEnd);
		mv.visitLabel(labelStart);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitLabel(labelEnd);
		
//		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.getType());
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		FieldVisitor fv;
		fv = cw.visitField(ACC_STATIC, declaration_Image.name, ImageSupport.ImageDesc, null, null);
		fv.visitEnd();
		
		if(declaration_Image.source != null) {
			declaration_Image.source.visit(this, arg);
		
			if(declaration_Image.xSize != null && declaration_Image.ySize != null) {
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			}
			else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
		}
		else {
			
			if(declaration_Image.xSize != null && declaration_Image.ySize != null) {
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);	
			}
			else {
//				mv.visitVarInsn(ILOAD, 9);
//				mv.visitVarInsn(ILOAD, 10);
				mv.visitLdcInsn(256);
				mv.visitLdcInsn(256);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "makeImage", "(II)Ljava/awt/image/BufferedImage;", false);
			
		}
		
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, "Ljava/awt/image/BufferedImage;");
		
//		throw new UnsupportedOperationException();
		return null;
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		
//		throw new UnsupportedOperationException();
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		
//		throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		
//		throw new UnsupportedOperationException();
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		FieldVisitor fv;
		fv = cw.visitField(ACC_STATIC, declaration_SourceSink.name, "Ljava/lang/String;", null, new String());
		fv.visitEnd();
		
		if(declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, "Ljava/lang/String;");
		}
		
//		throw new UnsupportedOperationException();
		return null;
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		mv.visitLdcInsn(expression_IntLit.value);
		
//		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if(expression_FunctionAppWithExprArg.function == Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "abs", "(I)I", false);
		}
		else if(expression_FunctionAppWithExprArg.function == Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "log", "(I)I", false);
		}
		
		
//		throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		
		if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_x) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_x", "(II)I", false);
		}
		else if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_y) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_y", "(II)I", false);
		}
		else if(expression_FunctionAppWithIndexArg.function == Kind.KW_polar_a) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", "(II)I", false);
		}
		else if(expression_FunctionAppWithIndexArg.function == Kind.KW_polar_r) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", "(II)I", false);
		}
		
//		throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		if(expression_PredefinedName.kind == Kind.KW_x) {
			mv.visitVarInsn(ILOAD, 1);
		}
		else if (expression_PredefinedName.kind == Kind.KW_y) {
			mv.visitVarInsn(ILOAD, 2);
		}
		else if (expression_PredefinedName.kind == Kind.KW_X) {
			mv.visitVarInsn(ILOAD, 3);
		}
		else if (expression_PredefinedName.kind == Kind.KW_Y) {
			mv.visitVarInsn(ILOAD, 4);
		}
		else if (expression_PredefinedName.kind == Kind.KW_r) {			
			mv.visitVarInsn(ILOAD, 5);
		}
		else if (expression_PredefinedName.kind == Kind.KW_a) {			
			mv.visitVarInsn(ILOAD, 6);
		}
		else if (expression_PredefinedName.kind == Kind.KW_R) {
			mv.visitVarInsn(ILOAD, 7);
		}
		else if (expression_PredefinedName.kind == Kind.KW_A) {
			mv.visitVarInsn(ILOAD, 8);
		}
		else if (expression_PredefinedName.kind == Kind.KW_DEF_X) {
			mv.visitLdcInsn(new Integer(256));
		}
		else if (expression_PredefinedName.kind == Kind.KW_DEF_Y) {
			mv.visitLdcInsn(new Integer(256));
		}
		else if (expression_PredefinedName.kind == Kind.KW_Z) {
			mv.visitLdcInsn(new Integer(16777215));
		}		
		
//		throw new UnsupportedOperationException();
		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		if(statement_Out.getDec().getType() == Type.INTEGER) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);		
		}
		else if(statement_Out.getDec().getType() == Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}
		else if(statement_Out.getDec().getType() == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Ljava/awt/image/BufferedImage;");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.IMAGE);
			statement_Out.sink.visit(this, arg);			
		}
		
//		throw new UnsupportedOperationException();
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		
		statement_In.source.visit(this, arg);
		if(statement_In.getDec().getType() == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		}
		else if(statement_In.getDec().getType() == Type.BOOLEAN){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
		}
		else if(statement_In.getDec().getType() == Type.IMAGE) {
			if(((Declaration_Image) statement_In.getDec()).xSize != null && ((Declaration_Image) statement_In.getDec()).ySize != null) {
				
				((Declaration_Image) statement_In.getDec()).xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				((Declaration_Image) statement_In.getDec()).ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				
			}
			else {
				
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
				
			}		
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");	
		}
		
//		throw new UnsupportedOperationException();
		return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@SuppressWarnings("unused")
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
		
		if(statement_Assign.lhs.getType() == Type.INTEGER || statement_Assign.lhs.getType() == Type.BOOLEAN) {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		else if(statement_Assign.lhs.getType() == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");		
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", "(Ljava/awt/image/BufferedImage;)I", false);
			mv.visitVarInsn(ISTORE, 3);
			
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", "(Ljava/awt/image/BufferedImage;)I", false);
			mv.visitVarInsn(ISTORE, 4);
			
			Label start2 = new Label();
			Label loop2 = new Label();
			Label end2 = new Label();
			
			// j = 0
			mv.visitLabel(start2);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 2);
			
			// j < Y
			mv.visitLabel(loop2);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitJumpInsn(IF_ICMPGE, end2);
			
			Label start1 = new Label();
			Label loop1 = new Label();
			Label end1 = new Label();
			
			// i = 0
			mv.visitLabel(start1);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			
			// i < X
			mv.visitLabel(loop1);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitJumpInsn(IF_ICMPGE, end1);
			
			// Load x and y
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", "(II)I", false);
			mv.visitVarInsn(ISTORE, 5);
			
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", "(II)I", false);
			mv.visitVarInsn(ISTORE, 6);
			
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			
			//increment & continue the X loop
			mv.visitIincInsn(1, 1);
			mv.visitJumpInsn(GOTO, loop1);
			mv.visitLabel(end1);
			
			//increment & continue the Y loop
			mv.visitIincInsn(2, 1);
			mv.visitJumpInsn(GOTO, loop2);
			mv.visitLabel(end2);
			
//			mv.visitInsn(ICONST_0);
//			   mv.visitVarInsn(ISTORE, 1);
//			   Label l1 = new Label();
//			   mv.visitLabel(l1);
//			   Label l2 = new Label();
//			   mv.visitJumpInsn(GOTO, l2);
//			   Label l3 = new Label();
//			   mv.visitLabel(l3);
//			  // mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
//			   mv.visitInsn(ICONST_0);
//			   mv.visitVarInsn(ISTORE, 2);
//			   Label l4 = new Label();
//			   mv.visitLabel(l4);
//			   Label l5 = new Label();
//			   mv.visitJumpInsn(GOTO, l5);
//			   Label l6 = new Label();
//			   mv.visitLabel(l6);
//			//   mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
//			   statement_Assign.e.visit(this, arg);
//			   statement_Assign.lhs.visit(this, arg);
//			   Label l7 = new Label();
//			   mv.visitLabel(l7);
//			   mv.visitIincInsn(2, 1);
//			   mv.visitLabel(l5);
//			   mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
//			   mv.visitVarInsn(ILOAD, 2);
//			   mv.visitVarInsn(ILOAD, 4);
//			   mv.visitJumpInsn(IF_ICMPLT, l6);
//			   Label l8 = new Label();
//			   mv.visitLabel(l8);
//			   mv.visitIincInsn(1, 1);
//			   mv.visitLabel(l2);
//			   //mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
//			   mv.visitVarInsn(ILOAD, 1);
//			   mv.visitVarInsn(ILOAD, 3);
//			   mv.visitJumpInsn(IF_ICMPLT, l3);
					
		}
		
		
//		throw new UnsupportedOperationException();
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		
		if(lhs.getType() == Type.INTEGER)
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
		else if(lhs.getType() == Type.BOOLEAN)
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
		else if(lhs.getType() == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, "Ljava/awt/image/BufferedImage;");
//			lhs.index.visit(this, arg);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "setPixel", "(ILjava/awt/image/BufferedImage;II)V", false);
		}
		
//		throw new UnsupportedOperationException();
		return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageFrame", "makeFrame", "(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
		mv.visitInsn(POP);
		
//		throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, "Ljava/lang/String;");
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "write", "(Ljava/awt/image/BufferedImage;Ljava/lang/String;)V", false);
		
//		throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		boolean flag = expression_BooleanLit.value;
		if (flag) {
			mv.visitInsn(ICONST_1);
		} else {
			mv.visitInsn(ICONST_0);
		}
		
//		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		
		if(expression_Ident.getType() == Type.INTEGER)
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
		else if(expression_Ident.getType() == Type.BOOLEAN)
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
		
//		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());
		return null;
	}

}
