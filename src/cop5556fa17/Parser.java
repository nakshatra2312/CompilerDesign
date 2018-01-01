package cop5556fa17;



import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.AST.*;

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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * @return 
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
		//throw new UnsupportedOperationException();
		
		Program prog=null;
		Token firstToken=t;
		Token ident=null;
		ArrayList<ASTNode> nodes= new ArrayList<ASTNode>();
		
		
		if(t.kind==Kind.IDENTIFIER)
		{
			ident=t;
			consume();
			while(1==1)
			{
				ASTNode node= Declaration();
				
				if(node==null)
				{
					node=Statement();
					
					if(node==null)
					{
						break;
					}
				}
				nodes.add(node);
				if(t.kind==Kind.SEMI)
				{
					consume();
				}
				else
				{
					throw new SyntaxException(t, " Syntax Exception found");
				}
			}
			prog = new Program(firstToken,ident,nodes);
		}
		else
		{
			throw new SyntaxException(t, " Syntax Exception found");
		}
		return prog;
	}
	
	Declaration Declaration() throws SyntaxException
	{
		Declaration declaration=null;
		declaration=VariableDeclaration();
		
		if(declaration==null)
		{
			declaration=ImageDeclaration();
			if(declaration==null)
			{
				declaration=SourceSinkDeclaration();
			}
		}
		
		return declaration; 
	}
	
	Declaration VariableDeclaration() throws SyntaxException
	{
		Token firstToken=t;
		Token type=VarType();
		Declaration dec=null;
		Token ident=null;
		Token op=null;
		Expression exp=null;
		
		if(type!=null)
		{
			if(t.kind==Kind.IDENTIFIER)
			{
				ident=t;
				consume();
				if(t.kind==Kind.OP_ASSIGN)
				{
					op=t;
					consume();
					exp=expression();
					if(exp!=null)
					{
						dec=new Declaration_Variable(firstToken,type,ident, exp);
						return dec;
					}
					else
					{
						throw new SyntaxException(t,"SYntax Exception");
					}
				}
				else
				{
					dec=new Declaration_Variable(firstToken,type,ident, exp);
					return dec;
				}
			}
			else
			{
				throw new SyntaxException(t,"SYntax Exception");
			}
			
		}
		return dec;
	}
	
	Token VarType() throws SyntaxException
	{
		Token firstToken=null;
		if(t.kind==Kind.KW_int)
			{
				firstToken=t;
				consume();
				return firstToken;
			}
			else if(t.kind==Kind.KW_boolean)
			{
				firstToken=t;
				consume();
				return firstToken;
			}
		return firstToken;

	}
	
	Declaration SourceSinkDeclaration() throws SyntaxException
	{
		Token firstToken=t;
		Token type=SourceSinkType();
		Declaration declaration=null;
	
		if(type!=null)
		{
			if(t.kind==Kind.IDENTIFIER)
			{
				Token ident=t;
				consume();
				if(t.kind==Kind.OP_ASSIGN)
				{
					Token op= t;
					consume();
					Source source=Source();
					if(source!=null)
					{
						declaration= new Declaration_SourceSink(firstToken, type,ident, source);
						return declaration;
					}
				}
				
			}
			
		}
		return declaration;
		
	}
	
	Source Source() throws SyntaxException
	{
		Token firstToken=t;
		Source source=null;
		
			if(t.kind==Kind.STRING_LITERAL)
			{
				consume();
				source= new Source_StringLiteral(firstToken, firstToken.getText());
				
				return source;
				
			}
			else if(t.kind==Kind.OP_AT)
			{
				consume();
				Expression exp=expression();
				
				if(exp!=null)
				{
					source=new Source_CommandLineParam(firstToken, exp);
				}
				return source;
				
			}
			else if(t.kind==Kind.IDENTIFIER)
			{
				source= new Source_Ident(firstToken, firstToken);
				consume();
				return source;
				
			}
			
			
		return source;
		
	}
	
	Token SourceSinkType() throws SyntaxException
	{
		Token firstToken=null;
		
			if(t.kind==Kind.KW_url)
			{
				firstToken=t;
				consume();
				return firstToken;
				
			}
			else if(t.kind==Kind.KW_file)
			{
				firstToken=t;
				consume();
				return firstToken;
				
			}
			return firstToken;
		
	}
	
	Declaration ImageDeclaration() throws SyntaxException
	{
		Token firstToken=t;
		Token toknname=null;
		Expression x= null;
		Expression y=null;
		Source source=null;
		Declaration imgdec=null;
		if(t.kind==Kind.KW_image)
		{
			
			
			consume();
			if(t.kind==Kind.LSQUARE)
			{
				boolean flag1=false;
				consume();
				x=expression();
				if(x!=null)
				{
					if(t.kind==Kind.COMMA)
					{
						consume();
						y=expression();
						if(y!=null)
						{
							if(t.kind==Kind.RSQUARE)
							{
								consume();
								flag1=true;
							}
						}
					
					}
				
				}
				
				if(flag1==false)
				{
					return null;
				}
					
					
			}	
			if(t.kind==Kind.IDENTIFIER)
			{
				toknname=t;
				consume();
				if(t.kind==Kind.OP_LARROW)
				{
					consume();
					source= Source();
					if(source!=null)
					{
						imgdec=new Declaration_Image(firstToken,x,y,toknname,source);
					}
					else
					{
						return null;
					}
					
				}
				
				imgdec=new Declaration_Image(firstToken,x,y,toknname,source);
			}

	
		}
		return imgdec;		
		
		
	}
	
	Statement Statement() throws SyntaxException
	{
		Token firstToken=t;
		Statement statement=null;
		LHS lhs=null;
		
		
		if(t.kind==Kind.IDENTIFIER)
		{
			
			consume();
			if(t.kind==Kind.OP_RARROW)
			{
				Token rarrow=t;
				consume();
				Sink sink=Sink();
				if(sink!=null)
				{
				 statement=new Statement_Out(firstToken, firstToken, sink);
				 return statement;
				}
			}
			else if(t.kind==Kind.OP_LARROW)
			{
				Token larrow=t;
				consume();
				Source source=Source();
				if(source!=null)
				{
				 statement=new Statement_In(firstToken, firstToken, source);
				 return statement;
				}
			}
			else if(t.kind==Kind.LSQUARE)
			{
				
				Index index=null;
				consume();
				if((index=LhsSelector())!=null)
				{
					if(t.kind==Kind.RSQUARE)
					{
						consume();
						lhs=new LHS(firstToken,firstToken,index);
						
						if(t.kind==Kind.OP_ASSIGN)
						{
							consume();
							Expression exp=expression();
							if(exp!=null)
							{
								statement=new Statement_Assign(firstToken, lhs, exp);
								return statement;
							}
						}
						
					}
				}
				
				
			}
			else if(t.kind==Kind.OP_ASSIGN)
			{
				lhs=new LHS(firstToken,firstToken,null);
				consume();
				Expression exp=expression();
				if(exp!=null)
				{
					statement=new Statement_Assign(firstToken, lhs, exp);
					return statement;
				}
			}
			throw new SyntaxException(t, "Syntax Exception");
		}
		return statement;
		
	}
	
/*	boolean ImageOutStatement() throws SyntaxException
	{
		if(t.kind==Kind.IDENTIFIER)
		{
			consume();
			if(t.kind==Kind.OP_RARROW)
			{
				consume();
				return Sink();
				
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
		
		return Sink();
	}*/
	
	Sink Sink() throws SyntaxException
	{
		Token firstToken=t;
		Sink sink=null;
		
		if(t.kind==Kind.IDENTIFIER)
		{
			sink=new Sink_Ident(firstToken,firstToken);
			consume();
			return sink;
			
		}
		else if(t.kind==Kind.KW_SCREEN)
		{
			sink= new Sink_SCREEN(firstToken);
			consume();
			return sink;
		}
		return sink;
	}
	
	/*boolean ImageInStatement() throws SyntaxException
	{
		if(t.kind==Kind.IDENTIFIER)
		{
			consume();
			if(t.kind==Kind.OP_LARROW)
			{
				consume();
				return Source();
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
		
		return Source();
	}
	
*/	
	
	/*Statement AssignmentStatement() throws SyntaxException
	{
		Token firstToken=t;
		Statement statement=null;
		LHS lhs=null;
		Expression exp=null;
		
		if((lhs=Lhs())!=null)
		{
			if(t.kind==Kind.OP_ASSIGN)
			{
				consume();
				if((exp=expression())!=null)
				{
					statement=new Statement_Assign(firstToken, lhs, exp);
					return statement;
					
				}
				
				
			}
			
			
		}
		return statement;
	}*/
	
	Expression OrExpression() throws SyntaxException
	{
		
		Token firstToken=t;
		Token op=null;
		Expression e0=null;
		Expression e1=null;
		e0=AndExpression();
		if(e0!=null)
		{
		
			while(t.kind==Kind.OP_OR)
			{
				op=t;
				consume();
				e1=AndExpression();
				if(e1!=null)
				{
					e0=new Expression_Binary(firstToken,e0,op,e1);
					//consume();
					
				}
				else
				{
					return null;
				}

				
			}
						
			
		}
		return e0;
	
	}
	
	Expression AndExpression() throws SyntaxException
	{
		Token firstToken=t;
		Token op=null;
		Expression e0=null;
		Expression e1=null;
		e0=EqExpression();
		if(e0!=null)
		{
		
			while(t.kind==Kind.OP_AND)
			{
				op=t;
				consume();
				e1=EqExpression();
				if(e1!=null)
				{
					e0=new Expression_Binary(firstToken,e0,op,e1);
					//consume();
					
				}
				else
				{
					return null;
				}

				
			}
						
			
		}
		return e0;
	
		
		
	}
	
	Expression EqExpression() throws SyntaxException
	{
		Token firstToken=t;
		Token op=null;
		Expression e0=null;
		Expression e1=null;
		e0=RelExpression();
		if(e0!=null)
		{
		
			while(t.kind==Kind.OP_EQ || t.kind==Kind.OP_NEQ )
			{
				op=t;
				consume();
				e1=RelExpression();
				if(e1!=null)
				{
					e0=new Expression_Binary(firstToken,e0,op,e1);
					//consume();
					
				}
				else
				{
					return null;
				}

				
			}
						
			
		}
		return e0;
	
		

	}
	
	Expression RelExpression() throws SyntaxException
	{
		Token firstToken=t;
		Token op=null;
		Expression e0=null;
		Expression e1=null;
		e0=AddExpression();
		if(e0!=null)
		{
		
			while(t.kind==Kind.OP_LT || t.kind==Kind.OP_GT || t.kind==Kind.OP_LE || t.kind==Kind.OP_GE)
			{
				op=t;
				consume();
				e1=AddExpression();
				if(e1!=null)
				{
					e0=new Expression_Binary(firstToken,e0,op,e1);
					//consume();
					
				}
				else
				{
					return null;
				}

				
			}
						
			
		}
		return e0;

		
	}
	
	Expression AddExpression() throws SyntaxException
	{
		Token firstToken=t;
		Token op=null;
		Expression e0=null;
		Expression e1=null;
		e0=MultExpression();
		if(e0!=null)
		{
		
			while(t.kind==Kind.OP_PLUS || t.kind==Kind.OP_MINUS)
			{
				op=t;
				consume();
				e1=MultExpression();
				if(e1!=null)
				{
					e0=new Expression_Binary(firstToken,e0,op,e1);
					//consume();
					
				}
				else
				{
					return null;
				}

				
			}
						
			
		}
		return e0;
	}
	
	Expression MultExpression() throws SyntaxException
	{
		Token firstToken=t;
		Token op=null;
		Expression e0=null;
		Expression e1=null;
		e0=UnaryExpression();
		if(e0!=null)
		{
		
			while(t.kind==Kind.OP_TIMES || t.kind==Kind.OP_DIV || t.kind==Kind.OP_MOD)
			{
				op=t;
				consume();
				e1=UnaryExpression();
				if(e1!=null)
				{
					e0=new Expression_Binary(firstToken,e0,op,e1);
					
					
				}
				else
				{
					return null;
				}

				
			}
						
			
		}
		return e0;
		
	}
	
	Expression UnaryExpression() throws SyntaxException
	{
		Token firstToken=t;
		Expression e0=null;
		Expression temp=null;
		
			if(t.kind==Kind.OP_PLUS)
			{
				consume();
				if((temp=UnaryExpression())!=null)
				{
					e0=new Expression_Unary(firstToken,firstToken,temp);
					
					return e0;
				}
				
				
			}
			else if(t.kind==Kind.OP_MINUS)
			{
				consume();
				if((temp=UnaryExpression())!=null)
				{
					e0=new Expression_Unary(firstToken,firstToken,temp);
					
					return e0;
				}
			
			}
			else if((temp=UnaryExpressionNotPlusMinus())!=null)
			{
				
				return temp;
			}
			return e0;
		
	}
	
	Expression UnaryExpressionNotPlusMinus() throws SyntaxException
	{
		Token firstToken=t;
		Expression e0=null;
		Expression temp=null;
		
		
			if(t.kind==Kind.OP_EXCL)
			{
				Token tempr=t;
				consume();
				
				if((temp=UnaryExpression())!=null)
				{
					
					return new Expression_Unary(tempr,tempr,temp);
				}
				else
				{
					return null;
				}
				
			}
			else if((temp=Primary())!=null)
			{
				return temp;
			}
			else if((temp=IdentOrPixelSelectorExpression())!=null)
			{
				return temp;
			}
			else if(t.kind==Kind.KW_x) 
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_y)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_r)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_a)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_X)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_Y)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_Z)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_A)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_R)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_DEF_X)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}
			else if(t.kind==Kind.KW_DEF_Y)
			{
				e0=new Expression_PredefinedName(firstToken,firstToken.kind);
				consume();
				return e0;
			}

return e0;
		
	}
	
	
	Expression Primary() throws SyntaxException
	{
		Expression e=null;
		Token firstToken=t;
		
			if(t.kind==Kind.INTEGER_LITERAL)
			{
				e=new Expression_IntLit(t,t.intVal());
				consume();
				
			}
			else if(t.kind==Kind.LPAREN)
			{
				consume();
				e=expression();
				
					if(t.kind==Kind.RPAREN)
					{
						consume();
						return e;
						
					}
					else
					{
						return null;
					}
					
				
			}
			else if((e=FunctionApplication())!=null)
			{
				return e;
			}
			else if(t.kind==Kind.BOOLEAN_LITERAL)
			{
				boolean val= t.getText().equals("true")?true:false;
				e=new Expression_BooleanLit(t,val);
				consume();
				return e;
				
			}
			return e;
			
		
		
	}
	
	Expression IdentOrPixelSelectorExpression() throws SyntaxException
	{
		Token firstToken=t;
		Index index=null;
		Expression e0=null;
		if(t.kind==Kind.IDENTIFIER)
		{
			consume();
			if(t.kind==Kind.LSQUARE)
			{
				consume();
				
				if((index=Selector())!=null)
				{
					if(t.kind==Kind.RSQUARE)
					{
						e0=new Expression_PixelSelector(firstToken,firstToken,index);
						consume();
						return e0;
						
					}
					
					
				}
				
				
			}
			e0=new Expression_Ident(firstToken,firstToken);
			
			
		}
		return e0;


	}
	
	/*LHS Lhs() throws SyntaxException
	{
		Token firstToken=t;
		LHS lhs=null;
		Index index=null;
		if(t.kind==Kind.IDENTIFIER)
		{
			consume();
			if(t.kind==Kind.LSQUARE)
			{
				consume();
				if((index=LhsSelector())!=null)
				{
					if(t.kind==Kind.RSQUARE)
					{
						lhs=new LHS(firstToken,t,index);
						consume();
						return lhs;
					}
				}
				
			}
			
			lhs=new LHS(firstToken,t,index);
		}
		return lhs;

	}*/
	
	Expression_FunctionApp FunctionApplication() throws SyntaxException
	{
		Token firstToken=t;
		Expression_FunctionApp e0=null;
		Expression exp=null;
		Index index=null;
		Token funcname=FunctionName();
		
		if(funcname!=null)
		{
			
				if(t.kind==Kind.LPAREN)
				{
					consume();
					if((exp=expression())!=null)
					{
					
						if(t.kind==Kind.RPAREN)
						{
							e0=new Expression_FunctionAppWithExprArg(firstToken,firstToken.kind, exp);
							consume();
							return e0;
							
						}
					}
						
					
					
				}
				else if(t.kind==Kind.LSQUARE)
				{
					consume();
					if((index=Selector())!=null)
					{
						if(t.kind==Kind.RSQUARE)
						{
							e0=new Expression_FunctionAppWithIndexArg(firstToken, firstToken.kind, index);
							consume();
							return e0;
							
						}
					
				}
			
		}
				
		}
		return e0;
		
	}

	Token FunctionName() throws SyntaxException
	{
		if(t.kind==Kind.KW_sin)
		{
			consume();
			return t;
		}
		if(t.kind==Kind.KW_cos)
		{
			consume();
			return t;
		}
		if(t.kind==Kind.KW_atan)
		{
			consume();
			return t;
		}
		if(t.kind==Kind.KW_abs)
		{
			consume();
			return t;
		}
		if(t.kind==Kind.KW_cart_x)
		{
			consume();
			return t;
		}
		if(t.kind==Kind.KW_cart_y)
		{
			consume();
			return t;
		}
		if(t.kind==Kind.KW_polar_a)
		{
			consume();
			return t;
		}
		if(t.kind==Kind.KW_polar_r)
		{
			consume();
			return t;
		}else
			return null;
	}
	
	Index LhsSelector() throws SyntaxException
	{
		Index index=null;
		

		if(t.kind==Kind.LSQUARE)
		{
			consume();
			
			index= XySelector();
			
			if(index==null)
			{
				index=RaSelector();
			}
			if(index!=null)
			{
				if(t.kind==Kind.RSQUARE)
				{
					consume();
					return index;
				}
			}
			
			
		}
		return null;
		
	}
	
	
	
	
	Index XySelector() throws SyntaxException
	{
		Token firstToken=t;
		Index index=null;
		Expression e0=null;
		Expression e1=null;
		
		if(t.kind==Kind.KW_x)
		{
			e0=new Expression_PredefinedName(t,t.kind);
			consume();
			if(t.kind==Kind.COMMA)
			{
				consume();
				if(t.kind==Kind.KW_y)
				{
					e1=new Expression_PredefinedName(t,t.kind);
					consume();
					index=new Index(firstToken,e0,e1);
					return index;
					
				}
				
				
			}
			
			
		}
		
		return index;
	}
	
	Index RaSelector() throws SyntaxException
	{
		Token firstToken=t;
		Index index=null;
		Expression e0=null;
		Expression e1=null;
		
		if(t.kind==Kind.KW_r)
		{
			e0=new Expression_PredefinedName(t,t.kind);
			consume();
			if(t.kind==Kind.COMMA)
			{
				consume();
				if(t.kind==Kind.KW_a)
				{
					e1=new Expression_PredefinedName(t,t.kind);
					consume();
					index=new Index(firstToken,e0,e1);
					return index;
					
				}
				
				
			}
			
			
		}
		
		return index;
	}
	
	Index Selector() throws SyntaxException
	{
		Token firstToken=t;
		Index index=null;
		Expression e0=null;
		Expression e1=null;
		
		if((e0=expression())!=null)
		{
			if(t.kind==Kind.COMMA)
			{
				consume();
				if((e1=expression())!=null)
				{
					index=new Index(firstToken,e0,e1);
					
					return index;
				}
				
				
			}
		}
		
		return index;
		
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
		//throw new UnsupportedOperationException();
		Token firstToken=t;
		Expression exp=null;
		Expression condition=null;
		Expression trueexp=null;
		Expression falseexp=null;
		
		if((condition=OrExpression())!=null)
		{
			if(t.kind==Kind.OP_Q)
			{
				consume();
				trueexp=expression();
				
				if(trueexp!=null)
				{
					if(t.kind==Kind.OP_COLON)
					{
						consume();
						falseexp=expression();
						
						if(falseexp!=null)
						{
							exp=new Expression_Conditional(firstToken,condition,trueexp, falseexp);
							return exp;
						}
						else
						{
							throw new SyntaxException(t, " Syntax Exception found");
						}
					}
					else
					{
						throw new SyntaxException(t, " Syntax Exception found");
					}
				}
				
				else
				{
					throw new SyntaxException(t, " Syntax Exception found");
				}
			}
			
			return condition;
		
			
		}
		else
		{
			throw new SyntaxException(t, " Syntax Exception found");
		}
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
	
	
	
	
	

	private void consume()
	{
		t = scanner.nextToken();
	}
}
