package cop5556fa17;

import java.net.URL;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
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
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	
       SymbolTable symboltab=new SymbolTable();
		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		

	
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
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		String name=declaration_Variable.name;
		Expression e = declaration_Variable.e;
		Type ExpressionType =null;
		if(e!=null)
		{
			e.visit(this, null);
			ExpressionType=e.utilType;
			
		}
		
		
		if(symboltab.lookupType(name)!=null)
		{
			throw new SemanticException(declaration_Variable.type, " Declaration_Variable1");
		}
		else
		{
			declaration_Variable.utilType=ExpressionType;
		}
		
		symboltab.insert(name, declaration_Variable);
		declaration_Variable.utilType=TypeUtils.getType(declaration_Variable.type);
		
		if(e!=null)
		{
		if(declaration_Variable.utilType!=ExpressionType)
		{
			throw new SemanticException(declaration_Variable.type, " Declaration_Variable2");
		}
		}

		
		
		return declaration_Variable;
		
		
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		Expression e0= expression_Binary.e0;
		if(e0!=null)
		{
			e0.visit(this, null);
		}
		
		Expression e1=expression_Binary.e1;
		if(e1!=null)
		{
			e1.visit(this, null);
		}
		Kind BinaryOp=null;
		if(expression_Binary.op!=null)
		{
			BinaryOp= expression_Binary.op;
		}
		
		
		if(BinaryOp ==Kind.OP_EQ ||BinaryOp ==Kind.OP_NEQ)
		{
			expression_Binary.utilType= TypeUtils.Type.BOOLEAN;
		}
		else if((BinaryOp==Kind.OP_GE || BinaryOp==Kind.OP_GT || BinaryOp==Kind.OP_LE || BinaryOp==Kind.OP_LT)&&(e0.utilType==TypeUtils.Type.INTEGER))
		{
			expression_Binary.utilType= TypeUtils.Type.BOOLEAN;
		}
		else if((BinaryOp==Kind.OP_AND|| BinaryOp==Kind.OP_OR )&&(e0.utilType==TypeUtils.Type.INTEGER || e0.utilType==TypeUtils.Type.BOOLEAN))
		{
			expression_Binary.utilType=e0.utilType;
		}
		else if((BinaryOp==Kind.OP_DIV || BinaryOp==Kind.OP_MINUS || BinaryOp==Kind.OP_MOD || BinaryOp==Kind.OP_PLUS || BinaryOp==Kind.OP_POWER || BinaryOp==Kind.OP_TIMES)&&(e0.utilType==TypeUtils.Type.INTEGER))
		{
			expression_Binary.utilType=TypeUtils.Type.INTEGER;
		}
		else
		{
			expression_Binary.utilType=null;
		}
		
		if((expression_Binary.utilType==null) || (e0.utilType!=e1.utilType))
		{
			throw new SemanticException(expression_Binary.firstToken, " Expression_Binary");
		}
		
		
		
		return expression_Binary;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		
		
		Kind operater= expression_Unary.op;
		Expression exp=expression_Unary.e;
		if(exp!=null)
		{
			exp.visit(this, null);
		}
		
		TypeUtils.Type t= exp.utilType;
		if((operater==Kind.OP_EXCL) && (t==TypeUtils.Type.BOOLEAN || t==TypeUtils.Type.INTEGER ))
		{
			expression_Unary.utilType=t;
		}
		else if(( operater==Kind.OP_PLUS || operater==Kind.OP_MINUS) && (t==TypeUtils.Type.INTEGER))
		{
			expression_Unary.utilType=TypeUtils.Type.INTEGER;
		}
		else
		{
			expression_Unary.utilType=null;
			
		}
		
		if(expression_Unary.utilType==null)
		{
			throw new SemanticException(expression_Unary.firstToken, " Expression_Unary");	
		}
		
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		Expression e0=index.e0;
		Expression e1=index.e1;
		if(e0!=null)
		{
			e0.visit(this, null);
		}
		if(e1!=null)
		{
			e1.visit(this, null);
		}
		
		if(e0.utilType!=TypeUtils.Type.INTEGER || e1.utilType!=TypeUtils.Type.INTEGER)
		{
			throw new SemanticException(index.firstToken, " Index");
		}
		
		
		index.setCartesian(!(e0.firstToken.kind==Kind.KW_r && e1.firstToken.kind==Kind.KW_a));
		
	
		
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		String name=expression_PixelSelector.name;
		
		
		Index index=expression_PixelSelector.index;
		
		
		
		
		 TypeUtils.Type nametype=symboltab.lookupType(name);
		 
		 if(nametype==TypeUtils.Type.IMAGE)
		 {
			 expression_PixelSelector.utilType=TypeUtils.Type.INTEGER;
		 }
		 else if(index==null)
		 {
			 expression_PixelSelector.utilType =nametype;
		 }
		 else
		 {
			 expression_PixelSelector.utilType=null; 
		 }
		if(index!=null)
		{
			index.visit(this, null);
		}
		
		if(expression_PixelSelector.utilType==null)
		{
			throw new SemanticException(expression_PixelSelector.firstToken, " visitExpression_PixelSelector");
		}
		return expression_PixelSelector;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		Expression condition= expression_Conditional.condition;
		if(condition!=null)
		{
		condition.visit(this, null);
		}
		Expression trueExp=expression_Conditional.trueExpression;
		if(trueExp!=null)
		{
		trueExp.visit(this, null);
		}
		Expression falseExp=expression_Conditional.falseExpression;
		if(falseExp!=null)
		{
		falseExp.visit(this, null);
		}
		
		if((condition.utilType!=TypeUtils.Type.BOOLEAN)||(trueExp.utilType!=falseExp.utilType))
		{
			throw new SemanticException(expression_Conditional.firstToken, " Declaration_Variable");
		}
		
		expression_Conditional.utilType=trueExp.utilType;
		
		return expression_Conditional; 
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		String name=declaration_Image.name;
		
		if(symboltab.lookupDec(name)!=null)
		{
			throw new SemanticException(declaration_Image.firstToken, " Declaration_Image");
		}
		symboltab.insert(name, declaration_Image);
		
		Source src = declaration_Image.source;
		Expression xsize= declaration_Image.xSize;
		Expression ysize=declaration_Image.ySize;
		if(src!=null)
		{
			src.visit(this, null);
		}
		if(xsize!=null)
		{
			xsize.visit(this, null);
		}
		if(ysize!=null)
		{
			ysize.visit(this, null);
		}
		
		declaration_Image.utilType=TypeUtils.Type.IMAGE;
		
		
		if(xsize!=null)
		{
			if(!((ysize!=null)&&(xsize.utilType==TypeUtils.Type.INTEGER && ysize.utilType==TypeUtils.Type.INTEGER)))
			{
				throw new SemanticException(declaration_Image.firstToken, " Declaration_Image");
			}
		}
		
		
			
		
		return declaration_Image;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String fileorUrl=source_StringLiteral.fileOrUrl;
		URL url=null;
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
		}
		


		
		return source_StringLiteral;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		
		Expression paramnum=source_CommandLineParam.paramNum;
		if(paramnum!=null)
		{
			paramnum.visit(this, null);
		}
		
		source_CommandLineParam.utilType=null;
		
		if(paramnum.utilType!=TypeUtils.Type.INTEGER)
		{
			throw new SemanticException(source_CommandLineParam.firstToken, " source_CommandLineParam");
		}
		
	
		
		
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		if(symboltab.lookupType(source_Ident.name)!=null)
		{
		source_Ident.utilType=symboltab.lookupType(source_Ident.name);
		}
		else
		{
			throw new SemanticException(source_Ident.firstToken, "source_Ident");
		}
		if(!(source_Ident.utilType==TypeUtils.Type.FILE || source_Ident.utilType==TypeUtils.Type.URL))
		{
			throw new SemanticException(source_Ident.firstToken, "source_Ident");
		}
		
		
		
		return source_Ident;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		String name= declaration_SourceSink.name;
		Source src= declaration_SourceSink.source;
		if(src!=null)
		{
			src.visit(this, null);
		}
		
		
		if(symboltab.lookupDec(name)!=null)
		{
			throw new SemanticException(declaration_SourceSink.firstToken, " Declaration_Image");
		}
		
		symboltab.insert(name, declaration_SourceSink);
		
		declaration_SourceSink.utilType=TypeUtils.getType(declaration_SourceSink.firstToken);
		
		if(!(src.utilType==declaration_SourceSink.utilType || src.utilType==null))
		{
			throw new SemanticException(declaration_SourceSink.firstToken, " Declaration_Image");
		}
		
		
		return declaration_SourceSink;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		expression_IntLit.utilType=TypeUtils.Type.INTEGER;
		
		return expression_IntLit;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		Expression argExp= expression_FunctionAppWithExprArg.arg;
		if(argExp!=null)
		{
			argExp.visit(this, null);
		}
		
		if(argExp.utilType!=TypeUtils.Type.INTEGER)
		{
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, " FunctionAppWithExprArg");
		}
		expression_FunctionAppWithExprArg.utilType=TypeUtils.Type.INTEGER;
		
		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		Index index=expression_FunctionAppWithIndexArg.arg;
		if(index!=null)
		{
			index.visit(this, null);
		}
		expression_FunctionAppWithIndexArg.utilType=TypeUtils.Type.INTEGER;
		return expression_FunctionAppWithIndexArg;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		expression_PredefinedName.utilType=TypeUtils.Type.INTEGER;
		
		return expression_PredefinedName;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		String name= statement_Out.name;
		Sink sink= statement_Out.sink;
		if(sink!=null)
		{
			sink.visit(this, null);
		}
		
		if(symboltab.lookupDec(name)==null)
		{
			throw new SemanticException(statement_Out.firstToken, " Statement_Out");
		}
		TypeUtils.Type nametype= symboltab.lookupType(name);
		TypeUtils.Type sinktype=sink.utilType;
		
		if(! (( (( nametype== TypeUtils.Type.INTEGER) || (nametype == TypeUtils.Type.BOOLEAN)) && (sinktype== TypeUtils.Type.SCREEN)) || ((nametype == TypeUtils.Type.IMAGE) && ((sinktype== TypeUtils.Type.FILE)|| (sinktype== TypeUtils.Type.SCREEN)))))
		{
			throw new SemanticException(statement_Out.firstToken, " Statement_Out");
		}
		
		
		
		statement_Out.setDec(symboltab.lookupDec(name));
		return statement_Out;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		statement_In.setDec(symboltab.lookupDec(statement_In.name));
		
		Source src= statement_In.source;
		if(src!=null)
		{
			src.visit(this, null);
		}
		
		
		
		/*if (!((symboltab.lookupDec(statement_In.name)!=null) && (symboltab.lookupType(statement_In.name)==src.utilType)))
		{
			throw new SemanticException(statement_In.firstToken, " Statement_In");
		}*/
		
		

		
		return statement_In;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		
		LHS lhs= statement_Assign.lhs;
		if(lhs!=null)
		{
			lhs.visit(this, null);
		}
		
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		
		Expression e= statement_Assign.e;
		if(e!=null)
		{
			e.visit(this, null);
		}
				
		if(statement_Assign.lhs.utilType!=statement_Assign.e.utilType)
		{
			if(statement_Assign.lhs.utilType==Type.IMAGE && statement_Assign.e.utilType== Type.INTEGER)
			{
			
			}
			else
			{
				throw new SemanticException(statement_Assign.firstToken, " Statement_Assign");
			}
		}		
				
		return statement_Assign;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		 
		Declaration dec=symboltab.lookupDec(lhs.name);
		if(dec==null)
		{
			throw new SemanticException(lhs.firstToken, " LHS");
		}
		
		lhs.setDec(symboltab.lookupDec(lhs.name));
		lhs.utilType=lhs.getDec().utilType;
		
		Index index= lhs.index;
		if(index!=null)
		{
			index.visit(this, null);
			lhs.setCartesian(index.isCartesian());
		}
		
		return lhs;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		sink_SCREEN.utilType=TypeUtils.Type.SCREEN;
		return sink_SCREEN;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		sink_Ident.utilType= symboltab.lookupType(sink_Ident.name);
		if(sink_Ident.utilType!=TypeUtils.Type.FILE)
		{
			throw new SemanticException(sink_Ident.firstToken, " sink_Ident");
		}
		
		
		return sink_Ident;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		
		
		expression_BooleanLit.utilType=TypeUtils.Type.BOOLEAN;
		
		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		String name =expression_Ident.name;
		expression_Ident.utilType=symboltab.lookupType(name);
		return expression_Ident;
	}
	
	

}
