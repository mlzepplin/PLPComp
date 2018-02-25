package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */


import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;

import java.util.ArrayList;

import static cop5556sp18.Scanner.Kind.*;


public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message){
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


	public Program parse() throws SyntaxException {
		Program program = program();
		matchEOF();

		//TODO - return Program
		return program;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token firstToken = t;
		Token progName = match(IDENTIFIER);
		Block block = block();

		//TODO - return Program
		return new Program(firstToken,progName,block);
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename};
	Kind[] firstStatement = { KW_input, KW_write, KW_while, KW_if, KW_show, KW_sleep,
			IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha};
	Kind[] type = { KW_int, KW_boolean, KW_image, KW_float, KW_filename};
	Kind[] color = {KW_red,KW_green,KW_blue,KW_alpha};
	Kind[] functionName = { KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a,
			KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_green,KW_blue,KW_alpha};// ALWAYS add color to this too
	Kind[] predefinedName = {KW_Z, KW_default_height, KW_default_width};

	public Block block() throws SyntaxException {

		Token firstToken = t;
		ArrayList<ASTNode> nodesList = new ArrayList<ASTNode>();

		match(LBRACE);

		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			 nodesList.add(declaration());
		} else if (isKind(firstStatement)) {
			 nodesList.add(statement());
		}
			match(SEMI);
		}
		match(RBRACE);

		//TODO - return Block
		return new Block(firstToken, nodesList);
	}


	public Declaration declaration() throws SyntaxException{
		Token firstToken = t;
		Token typeName ;
		Token name ;
		Expression width = null;
		Expression height = null;

		//Type
		if(isKind(type)){

			if(isKind(KW_image)){
				typeName = match(KW_image);
				name = match(IDENTIFIER);

				//the longer route
				if(isKind(LSQUARE)){
					match(LSQUARE);
					width = expression();
					match(COMMA);
					height = expression();
					match(RSQUARE);
				}

			}
			else{
				typeName = match(type);
				name = match(IDENTIFIER);
			}

		}
		else throw new SyntaxException(t,"from declaration()");
		return new Declaration(firstToken,typeName, name, width, height);
	}

	public Statement statement() throws SyntaxException{
		Token firstToken = t;
		if(isKind(KW_input)) return statementInput(firstToken);
		else if(isKind(KW_write)) return statementWrite(firstToken);
		else if(isKind(IDENTIFIER)||isKind(color)) return statementAssignment(firstToken);
		else if(isKind(KW_while)) return statementWhile(firstToken);
		else if(isKind(KW_if)) return statementIf(firstToken);
		else if(isKind(KW_show)) return statementShow(firstToken);
		else if(isKind(KW_sleep)) return statementSleep(firstToken);
		else throw new SyntaxException(t,"from statement()");
	}

	public Statement statementInput(Token firstToken) throws SyntaxException{
		match(KW_input);
		Token destName = match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		Expression expression = expression();
		return new StatementInput(firstToken,destName,expression);
	}
	public Statement statementWrite(Token firstToken) throws SyntaxException{
		match(KW_write);
		Token sourceName = match(IDENTIFIER);
		match(KW_to);
		Token destName = match(IDENTIFIER);
		return new StatementWrite(firstToken,sourceName,destName);
	}
	public Statement statementAssignment(Token firstToken) throws SyntaxException{
		LHS lhs = lhs();
		match(OP_ASSIGN);
		Expression expression = expression();
		return new StatementAssign(firstToken,lhs,expression);
	}
	public Statement statementWhile(Token firstToken) throws SyntaxException{
		match(KW_while);
		match(LPAREN);
		Expression expression = expression();
		match(RPAREN);
		Block block = block();
		return new StatementWhile(firstToken,expression,block);

	}
	public Statement statementIf(Token firstToken) throws SyntaxException{
		match(KW_if);
		match(LPAREN);
		Expression expression = expression();
		match(RPAREN);
		Block block = block();
		return new StatementIf(firstToken,expression,block);

	}
	public Statement statementShow(Token firstToken) throws SyntaxException{
		match(KW_show);
		Expression expression = expression();
		return new StatementShow(firstToken,expression);
	}
	public Statement statementSleep(Token firstToken) throws SyntaxException{
		match(KW_sleep);
		Expression expression = expression();
		return new StatementSleep(firstToken,expression);
	}
	public LHS lhs() throws SyntaxException{
		Token firstToken = t;
		Token name;
		PixelSelector pixelSelector;
		if(isKind(IDENTIFIER)){
			name = match(IDENTIFIER);
			if(isKind(LSQUARE)){
				pixelSelector = pixelSelector();
				return new LHSPixel(firstToken, name, pixelSelector);
			}
			return new LHSIdent(firstToken,name);
		}
		else{
			Token c = match(color);
			match(LPAREN);
			name = match(IDENTIFIER);
			pixelSelector = pixelSelector();
			match(RPAREN);
			return new LHSSample(firstToken, name, pixelSelector, c);
		}

	}





	public Expression expression() throws SyntaxException{
			Token firstToken = t;
			Expression expression = orExpression();
			if(isKind(OP_QUESTION)) {
				match(OP_QUESTION);
				Expression expression1 = expression();
				match(OP_COLON);
				Expression expression2 = expression();
				expression = new ExpressionConditional(firstToken, expression, expression1,expression2);
			}
			return expression;
	}

	public Expression orExpression() throws SyntaxException{
		Token firstToken = t;
		Expression expression = andExpression();

		while(isKind(OP_OR)){
			Token op = match(OP_OR);
			Expression rightExpression = andExpression();
			expression = new ExpressionBinary(firstToken,expression,op,rightExpression);
		}
		return expression;
	}

	public Expression andExpression() throws SyntaxException{
		Token firstToken = t;
		Expression expression = eqExpression();
		while(isKind(OP_AND)){
			Token op = match(OP_AND);
			Expression rightExpression = eqExpression();
			expression = new ExpressionBinary(firstToken, expression, op, rightExpression);
		}
		return expression;
	}

	public Expression eqExpression() throws SyntaxException{
		Token firstToken = t;
		Token op;
		Expression expression = realExpression();
		Expression rightExpression;
		while(isKind(OP_EQ)||isKind(OP_NEQ)){

			if(isKind(OP_EQ)){
				op = match(OP_EQ);
				rightExpression = realExpression();
				expression = new ExpressionBinary(firstToken,expression,op,rightExpression);
			}
			else if(isKind(OP_NEQ)){
				op = match(OP_NEQ);
				rightExpression = realExpression();
				expression = new ExpressionBinary(firstToken,expression,op,rightExpression);
			}


		}
		return expression;
	}
	public Expression realExpression() throws SyntaxException{
		Token firstToken = t;
		Expression expression = addExpression();
		Token op;
		while(isKind(OP_LT)||isKind(OP_GT)||isKind(OP_LE)||isKind(OP_GE)){
			if(isKind(OP_LT)){
				op=match(OP_LT);
				expression = new ExpressionBinary(firstToken,expression,op,addExpression());

			}
			else if(isKind(OP_GT)){
				op=match(OP_GT);
				expression = new ExpressionBinary(firstToken,expression,op,addExpression());
			}
			else if(isKind(OP_LE)){
				op=match(OP_LE);
				expression = new ExpressionBinary(firstToken,expression,op,addExpression());

			}
			else if(isKind(OP_GE)){
				op=match(OP_GE);
				expression = new ExpressionBinary(firstToken,expression,op,addExpression());
			}
		}
		return expression;

	}

	public Expression addExpression() throws SyntaxException{
		Token firstToken =t;
		Token op;
		Expression expression = multExpression();
		while(isKind(OP_PLUS)||isKind(OP_MINUS)){

			if(isKind(OP_PLUS)){
				op= match(OP_PLUS);
				expression = new ExpressionBinary(firstToken,expression,op,multExpression());
			}
			else if(isKind(OP_MINUS)){
				op= match(OP_MINUS);
				expression = new ExpressionBinary(firstToken,expression,op,multExpression());
			}

		}
		return expression;
	}

	public Expression multExpression() throws SyntaxException{
		Token firstToken = t;
		Token op;
		Expression expression = powerExpression();
		while(isKind(OP_TIMES)||isKind(OP_DIV)||isKind(OP_MOD)){

			if(isKind(OP_TIMES)){
				op = match(OP_TIMES);
				expression = new ExpressionBinary(firstToken,expression,op,powerExpression());
			}
			else if(isKind(OP_DIV)){
				op=match(OP_DIV);
				expression = new ExpressionBinary(firstToken,expression,op,powerExpression());
			}
			else if(isKind(OP_MOD)){
				op=match(OP_MOD);
				expression = new ExpressionBinary(firstToken,expression,op,powerExpression());
			}

		}
		return expression;

	}

	public Expression powerExpression() throws SyntaxException{
		//if not this , error will be thrown by subsequent children calls
		//of unaryExpression, and otherwise it's just a recursive call
		Token firstToken = t;
		Expression expression = unaryExpression();
		if(isKind(OP_POWER)){
			Token op = match(OP_POWER);
			expression = new ExpressionBinary(firstToken,expression,op,powerExpression());
		}
		return expression;
	}

	public Expression unaryExpression() throws SyntaxException{
		Token firstToken = t ;
		Token op;
		if(isKind(OP_PLUS)){
			op = match(OP_PLUS);
			return new ExpressionUnary(firstToken,op,unaryExpression());
		} else if (isKind(OP_MINUS)) {
			op = match(OP_MINUS);
			return new ExpressionUnary(firstToken,op,unaryExpression());
		}
		else{
			//if it's not unaryExpressionNotPlusMinus too, error is being thrown
			//by its subsequent calls
			return unaryExpressionNotPlusMinus();
		}
	}

	public Expression unaryExpressionNotPlusMinus() throws SyntaxException{
		Token firstToken =t;
		if(isKind(OP_EXCLAMATION)||isKind()){
			Token op = match(OP_EXCLAMATION);
			return new ExpressionUnary(firstToken,op,unaryExpression());
		}
		else{
			//NOTE- primary will throw error if it is anything but primary
			return primary();
		}
	}


	public Expression primary() throws SyntaxException{
			Token firstToken =t;
			if(isKind(INTEGER_LITERAL)){
				Token intL = match(INTEGER_LITERAL);
				return new ExpressionIntegerLiteral(firstToken,intL);
			}
			else if(isKind(BOOLEAN_LITERAL)){
				Token boolL = match(BOOLEAN_LITERAL);
				return new ExpressionBooleanLiteral(firstToken,boolL);
			}
			else if(isKind(FLOAT_LITERAL)){
				Token floatL = match(FLOAT_LITERAL);
				return new ExpressionFloatLiteral(firstToken,floatL);
			}
			else if(isKind(LPAREN)){
				match(LPAREN);
				Expression expression = expression();
				match(RPAREN);
				return expression;
			}
			else if(isKind(functionName)){
				return functionApplication();
			}
			else if(isKind(IDENTIFIER)){
				Token name = match(IDENTIFIER);
				if(isKind(LSQUARE)) return new ExpressionPixel(firstToken,name, pixelSelector());
				return new ExpressionIdent(firstToken,name);
			}
			else if(isKind(predefinedName)){
				Token name = match(predefinedName);
				return new ExpressionPredefinedName(firstToken, name);
			}
			else if(isKind(LPIXEL)) {
				return pixelConstructor();
			}
			else throw new SyntaxException(t,"from primary()");
	}

	public ExpressionPixelConstructor pixelConstructor() throws SyntaxException{
			Token firstToken = t;
			match(LPIXEL);
			Expression a = expression();
			match(COMMA);
			Expression b = expression();
			match(COMMA);
			Expression c =expression();
			match(COMMA);
			Expression d =expression();
			match(RPIXEL);
			return new ExpressionPixelConstructor(firstToken,a,b,c,d);

	}

	public PixelSelector pixelSelector() throws SyntaxException{
		Token firstToken = t;
		match(LSQUARE);
		Expression expression0 = expression();
		match(COMMA);
		Expression expression1 = expression();
		match(RSQUARE);
		return new PixelSelector(firstToken,expression0,expression1);
	}


	public Expression functionApplication() throws SyntaxException{
		Token firstToken = t;

		Token name = match(functionName);
		if(isKind(LPAREN)){
			match(LPAREN);
			Expression expression = expression();
			match(RPAREN);
			return new ExpressionFunctionAppWithExpressionArg(firstToken,name, expression);
		}
		else if(isKind(LSQUARE)){
			match(LSQUARE);
			Expression a = expression();
			match(COMMA);
			Expression b =expression();
			match(RSQUARE);
			return new ExpressionFunctionAppWithPixel(firstToken,name,a,b);
		}
		else{
			throw new SyntaxException(t,"from functionApplication()");
		}


	}


	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"from match()"); //TODO  give a better error message!
	}

	//Override
	private Token match(Kind... kinds) throws SyntaxException {
		Token tmp = t;
		//this method takes care of isKind and match in conjunction
		for (Kind k : kinds) {
			if (k == t.kind){
				return match(k);
			}
		}
		throw new SyntaxException(t,"from overridden match()");

	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"from consume()"); //TODO  give a better error message!
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"from matchEOF()"); //TODO  give a better error message!
	}


}

