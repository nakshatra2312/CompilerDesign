package cop5556fa17;

import java.net.URL;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
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
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {
	public String get_fieldType(Type type) {
		
		if (type==Type.INTEGER) 
		{
			return "I";
		}else if (type == Type.BOOLEAN)
		{
			return  "Z";
		}else if (type == Type.FILE || type == Type.URL) 
		{
			return "Ljava/lang/String;";
		}else if (type == Type.IMAGE) 
		{
			return ImageSupport.ImageDesc;
		}else 
		{
			return "Ljava/lang/String;";
		}
		
		}

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
	
	
	
	int x_slot;
	


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
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
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
		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
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
		//throw new UnsupportedOperationException();
	
		
		Type type= TypeUtils.getType(declaration_Variable.type); 
		
		String fieldName= declaration_Variable.name;
		String fieldType=null;
		Object initVal=null;
		
		if(type==Type.INTEGER)
		{
			fieldType="I";
			initVal= new Integer(0);
		}
		else if(type== Type.BOOLEAN){
			fieldType="Z";
			initVal= new Boolean(true);
		}
		
		FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, initVal);
		fv.visitEnd();
		
		Expression e =declaration_Variable.e;
		
		if(e!=null)
		{
			e.visit(this,arg);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		
		Expression e0= expression_Binary.e0;
		Expression e1= expression_Binary.e1;
		
		if(e0!=null)
		{
			e0.visit(this, arg);
			
		}
		
		if(e1!=null)
		{
			e1.visit(this, arg);
		}
		
		Label startLabel = new Label();
		Label endLabel = new Label();
		
		Kind op =expression_Binary.op;
		
		if(op== Kind.OP_DIV)
		{
			mv.visitInsn(IDIV);
		}
		else if(op==Kind.OP_MINUS)
		{
			mv.visitInsn(ISUB);
		}
		else if(op==Kind.OP_PLUS)
		{
			mv.visitInsn(IADD);
		}
		else if(op==Kind.OP_MOD)
		{
			mv.visitInsn(IREM);
		}
		else if(op==Kind.OP_TIMES)
		{
			mv.visitInsn(IMUL);
		}
		else if(op==Kind.OP_AND)
		{
			mv.visitInsn(IAND);
		}
		else if(op==Kind.OP_OR)
		{
			mv.visitInsn(IOR);
		}
		else if(op==Kind.OP_GE)
		{
			mv.visitJumpInsn(IF_ICMPGE, startLabel);
			mv.visitLdcInsn(false);
		}
		else if(op==Kind.OP_GT)
		{
			mv.visitJumpInsn(IF_ICMPGT, startLabel);
			mv.visitLdcInsn(false);
		}
		else if(op==Kind.OP_LT)
		{
			mv.visitJumpInsn(IF_ICMPLT, startLabel);
			mv.visitLdcInsn(false);
			
		}
		else if(op==Kind.OP_LE)
		{
			mv.visitJumpInsn(IF_ICMPLE, startLabel);
			mv.visitLdcInsn(false);
		}
		else if(op==Kind.OP_EQ)
		{
			mv.visitJumpInsn(IF_ICMPEQ, startLabel);
			mv.visitLdcInsn(false);
		}
		else if(op==Kind.OP_NEQ)
		{
			mv.visitJumpInsn(IF_ICMPNE, startLabel);
			mv.visitLdcInsn(false);
		}
		
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(startLabel);
		mv.visitLdcInsn(true);
		mv.visitLabel(endLabel);
		
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.utilType);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		
		
		Expression e=expression_Unary.e;
		Kind op = expression_Unary.op;
		
	
		
		e.visit(this, arg);
		 
		Label startLabel= new Label();
		Label endLabel= new Label();
		if(op== Kind.OP_MINUS)
		{
			
			mv.visitInsn(INEG);
		}
		else if(op== Kind.OP_EXCL)
		{
			
			if(e.utilType== Type.INTEGER)
			{
				mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitInsn(IXOR);
			}
			else if(e.utilType==Type.BOOLEAN)
			{
				mv.visitJumpInsn(IFEQ, startLabel);
				mv.visitLdcInsn(new Integer(0));
				
			}
			
		}
		
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(startLabel);
		mv.visitLdcInsn(new Integer(1));
		mv.visitLabel(endLabel);
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.utilType);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		
		
		Expression e0= index.e0;
		Expression e1= index.e1;
		
		e0.visit(this, arg);
		e1.visit(this, arg);
		
		if(!index.isCartesian())
		{
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		
		
		}
		
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		
		String imageName=expression_PixelSelector.name;
		
		mv.visitFieldInsn(GETSTATIC, className, imageName, ImageSupport.ImageDesc);
		Index index= expression_PixelSelector.index;
//		Expression e0= expression_PixelSelector.index.e0;
//		Expression e1= expression_PixelSelector.index.e1;
//		
//		e0.visit(this, arg);
//		e1.visit(this, arg);
		
		index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getPixel" , ImageSupport.getPixelSig,false);
		
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		
		
		Expression conditionexp= expression_Conditional.condition;
		Expression trueexp=expression_Conditional.trueExpression;
		Expression falseexp= expression_Conditional.falseExpression;
		
		Label startLabel= new Label();
		Label endLabel = new Label();
		
		conditionexp.visit(this, arg);
		

		mv.visitLdcInsn(true);
		
		
		mv.visitJumpInsn(IF_ICMPEQ, startLabel);
		falseexp.visit(this, arg);
		
		
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(startLabel);
		trueexp.visit(this, arg);
		mv.visitLabel(endLabel);
		
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.utilType);
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		String fieldName= declaration_Image.name;
		Type type= declaration_Image.utilType;
		String fieldType= get_fieldType(type);
		FieldVisitor fv = cw.visitField(ACC_STATIC,fieldName, fieldType, null, null);
		fv.visitEnd();
		
		Expression xexp=declaration_Image.xSize;
		Expression yexp=declaration_Image.ySize;
		Source source= declaration_Image.source;
		
		if (source == null) 
		{
			if (declaration_Image.xSize == null) 
			{
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ISTORE, 3);
				mv.visitLdcInsn(256);
			}
			else 
			{
			
			
			declaration_Image.xSize.visit(this, arg);
			mv.visitInsn(DUP);
			mv.visitVarInsn(ISTORE,3);
			}
			
			if (declaration_Image.ySize != null) 
			{
			declaration_Image.ySize.visit(this, arg);
			mv.visitInsn(DUP);
			mv.visitVarInsn(ISTORE, 4);
			}
			else 
			{
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 4);
			mv.visitLdcInsn(256);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
			
			
			
		}
		else 
		{

			source.visit(this, arg);
			
			if (xexp == null) 
			{
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ISTORE, 3);
				mv.visitInsn(ACONST_NULL);
			}
			else 
			{
			
			
			xexp.visit(this, arg);
			mv.visitInsn(DUP);
			mv.visitVarInsn(ISTORE,3);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false); 
			}
			
			if (yexp == null) 
			{
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ISTORE, 4);
				mv.visitInsn(ACONST_NULL);
			}
			else 
			{	
			

				yexp.visit(this, arg);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE, 4);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false); 
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			
			
		}
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, ImageSupport.ImageDesc);
			return null;

		
		
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		
		String fileorUrl=source_StringLiteral.fileOrUrl;
		/*URL url=null;
		try
		{
			url=new URL(fileorUrl);
		}
		catch(Exception e)
		{
			
		}
		if(url==null)
		{
			source_StringLiteral.utilType=TypeUtils.Type.FILE;
		}
		else
		{
			source_StringLiteral.utilType=TypeUtils.Type.URL;
		}*/
		
		
		mv.visitLdcInsn(fileorUrl);
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		
		String name =source_Ident.name;
		Type type=source_Ident.utilType;
		String fieldtype=get_fieldType(type);
		
		/*if(type==Type.INTEGER)
		{
			fieldtype="I";
		}
		else if(type==Type.BOOLEAN)
		{
			fieldtype="Z";
		}
		else if(type==Type.IMAGE)
		{
			fieldtype=ImageSupport.ImageDesc;
		}*/
		
		mv.visitFieldInsn(GETSTATIC, className, name,fieldtype);
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		
		String fieldName= declaration_SourceSink.name;
		Type type=declaration_SourceSink.utilType;
		String fieldtype=get_fieldType(type);;
		/*if(type==Type.INTEGER)
		{
			fieldtype="I";
		}
		else if(type==Type.BOOLEAN)
		{
			fieldtype="Z";
		}
		else if(type==Type.IMAGE)
		{
			fieldtype=ImageSupport.ImageDesc;
		}*/
		FieldVisitor fv = cw.visitField(ACC_STATIC,fieldName, fieldtype, null, null);
		fv.visitEnd();
		
		Source source=declaration_SourceSink.source;
		
		if(source!=null)
		{
			source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldtype);
		}
		
		
		return null;
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		
		Integer val = expression_IntLit.value;
		mv.visitLdcInsn(new Integer(val));
		
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		
		Expression e= expression_FunctionAppWithExprArg.arg;
		e.visit(this, arg);
		
		Kind token = expression_FunctionAppWithExprArg.function;
		
		if(token == Kind.KW_abs)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
		/*if(token==Kind.KW_log)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log",   RuntimeFunctions.logSig, false);
		}*/
		
		
		
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		
		Index index=expression_FunctionAppWithIndexArg.arg;
		Expression e0=index.e0;
		Expression e1=index.e1;
		
		e0.visit(this, arg);
		e1.visit(this, arg);
		
		Kind token=expression_FunctionAppWithIndexArg.function;
		
		if(token==Kind.KW_cart_x)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		}
		if(token==Kind.KW_cart_y)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		if(token==Kind.KW_polar_r)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		if(token==Kind.KW_polar_a)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		
		
		
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		
		//mv.visitFieldInsn(GETSTATIC, className, fieldname, ImageSupport.ImageDesc);
		Kind token=expression_PredefinedName.kind;
		
		
		if(token==Kind.KW_x)
		{
		mv.visitVarInsn(ILOAD,1);	
		}
		if(token==Kind.KW_y)
		{
			mv.visitVarInsn(ILOAD,2);
		}
		if(token==Kind.KW_X)
		{
			mv.visitVarInsn(ILOAD,3);
		}
		if(token==Kind.KW_Y)
		{
			mv.visitVarInsn(ILOAD,4);
		}
		if(token==Kind.KW_r)
		{
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		
		}
		if(token==Kind.KW_a)
		{
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			
		}
		if(token==Kind.KW_R)
		{
			mv.visitVarInsn(ILOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		if(token==Kind.KW_A)
		{
			mv.visitVarInsn(ILOAD,3);
			mv.visitVarInsn(ILOAD,4);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a",   RuntimeFunctions.polar_aSig, false);
		}
		if(token==Kind.KW_DEF_X)
		{
			mv.visitLdcInsn(new Integer(256));
		}
		if(token==Kind.KW_DEF_Y)
		{
			mv.visitLdcInsn(new Integer(256));
		}
		if(token==Kind.KW_Z)
		{
			mv.visitLdcInsn(new Integer(16777215)); 
			
		}
		
		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		//throw new UnsupportedOperationException();
		
		
		String name = statement_Out.name;
		Type type = statement_Out.getDec().utilType;
		String fieldType = get_fieldType(type);
		
		
		if(type==Type.INTEGER)
		{
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, name, fieldType);
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "("+fieldType+")V", false);
			
			
		}
		else if(type== Type.BOOLEAN){
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, name, fieldType);
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "("+fieldType+")V", false);
			
			
		}
		else if(type==Type.IMAGE)
		{
			mv.visitFieldInsn(GETSTATIC, className, name, fieldType);
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			Sink sink=statement_Out.sink;
			sink.visit(this, arg);  
		}
		
		//CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.utilType);
		
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
		//throw new UnsupportedOperationException();
		Source source= statement_In.source;
		source.visit(this, arg);
		
		
		Type type= statement_In.getDec().utilType; 
		String fieldName= statement_In.name;
		String fieldType=get_fieldType(type);
		
		Label startLabel= new Label();
		Label endLabel= new Label();
		
		
		if(type==Type.INTEGER)
		{
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false); 
		}
		else if(type== Type.BOOLEAN){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
		}
		else if(type==Type.IMAGE)
		{
			
			mv.visitVarInsn(ILOAD, 3);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(IF_ICMPEQ, startLabel);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitJumpInsn(GOTO, endLabel);
			mv.visitLabel(startLabel);
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitLabel(endLabel);
			
		}
		
		mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		
		return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
		//throw new UnsupportedOperationException();
		
		Expression e= statement_Assign.e;
		LHS lhs= statement_Assign.lhs;
		Type type= lhs.utilType;
		String fieldType= get_fieldType(type);
		
		
		
		if(type==Type.INTEGER || type== Type.BOOLEAN)
		{
			e.visit(this, arg);
			lhs.visit(this, arg);
		}
		else if(type==Type.IMAGE)
		{
			String name= statement_Assign.lhs.name;
			mv.visitFieldInsn(GETSTATIC, className, name, fieldType);
			mv.visitInsn(DUP);
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			
			mv.visitVarInsn(ISTORE, 3);
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			
			mv.visitVarInsn(ISTORE, 4);
			
			
			mv.visitInsn(ICONST_0);				//i= 0 
			mv.visitVarInsn(ISTORE, 1);
			
			
			Label label1 = new Label();
			
			mv.visitJumpInsn(GOTO, label1);
			
			Label label2 = new Label();
			mv.visitLabel(label2);
			
			//mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
			
			mv.visitInsn(ICONST_0);				//j=0
			mv.visitVarInsn(ISTORE, 2);
			
			Label label3 = new Label();
			mv.visitJumpInsn(GOTO, label3);
			
			Label label4 = new Label();
			mv.visitLabel(label4);
			
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
			
			e.visit(this, arg);
			lhs.visit(this, arg);

			mv.visitIincInsn(2, 1);
			
			
			mv.visitLabel(label3);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			
			mv.visitVarInsn(ILOAD, 2);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitJumpInsn(IF_ICMPLT, label4);

			mv.visitIincInsn(1, 1);
			
			
			mv.visitLabel(label1);
			
			mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
			
			mv.visitVarInsn(ILOAD, 1); 
			mv.visitVarInsn(ILOAD, 3);
			mv.visitJumpInsn(IF_ICMPLT, label2);					
			
		}
		
		
		
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		//throw new UnsupportedOperationException();
		
		
		Type type= lhs.utilType;
		
		String fieldname= lhs.name;
		String fieldType= get_fieldType(type);
				
		if(type==Type.INTEGER || type== Type.BOOLEAN)
		{
			
			mv.visitFieldInsn(PUTSTATIC, className, fieldname, fieldType);
			
		}
		
		else if(type== Type.IMAGE)
		{
			mv.visitFieldInsn(GETSTATIC, className , lhs.name, fieldType);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
			
		}
			
		
		
		return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		//throw new UnsupportedOperationException();
		
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
	}
	

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		//throw new UnsupportedOperationException();
		String filename=sink_Ident.name;
		String fieldType= get_fieldType(sink_Ident.utilType);
		
		mv.visitFieldInsn(GETSTATIC, className, filename, fieldType);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		//throw new UnsupportedOperationException();
		
		boolean value= expression_BooleanLit.value;
		/*if(value==true)
		{
			mv.visitLdcInsn(new Integer(1));
		}
		else
		{
			mv.visitLdcInsn(new Integer(0));
		}*/
		
		mv.visitLdcInsn(new Boolean(value));
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}
	
	

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		//throw new UnsupportedOperationException();
		
		String name= expression_Ident.name;
		
		String fieldType=get_fieldType(expression_Ident.utilType);
		
		/*if(type==Type.INTEGER)
		{
			
			fieldType="I";
			
		}
		else if(type== Type.BOOLEAN){
			
			fieldType="Z";
			
		}*/
		
		
		mv.visitFieldInsn(GETSTATIC, className, name, fieldType);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.utilType);
		
		
		return null;
	}
	
	

}


