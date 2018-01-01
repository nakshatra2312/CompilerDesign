package cop5556fa17.AST;

import cop5556fa17.TypeUtils;
import cop5556fa17.Scanner.Token;

public abstract class Declaration extends ASTNode {
	
	public TypeUtils.Type utilType;

	public Declaration(Token firstToken) {
		super(firstToken);
		//this.utilType=TypeUtils.getType(firstToken);
	}



}
