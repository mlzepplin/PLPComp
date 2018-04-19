package cop5556sp18;

import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.*;

import java.util.HashSet;
import java.util.Set;

public class TypeChecker implements ASTVisitor {

	public SymbolTable symbolTable = new SymbolTable();
	public Set<Kind> s1 = new HashSet<Kind>();
	public Set<Kind> s2 = new HashSet<Kind>();
	public Set<Kind> s3 = new HashSet<Kind>();

	public Set<Kind> f1 = new HashSet<Kind>();
	public Set<Kind> f2 = new HashSet<Kind>();
	public Set<Kind> f3 = new HashSet<Kind>();

	TypeChecker() {



		s1.add(Kind.OP_PLUS);
		s1.add(Kind.OP_MINUS);
		s1.add(Kind.OP_TIMES);
		s1.add(Kind.OP_POWER);
		s1.add(Kind.OP_DIV);

		s2.add(Kind.OP_EQ);
		s2.add(Kind.OP_NEQ);
		s2.add(Kind.OP_GT);
		s2.add(Kind.OP_LT);
		s2.add(Kind.OP_LE);
		s2.add(Kind.OP_GE);

		s3.add(Kind.OP_AND);
		s3.add(Kind.OP_OR);


		f1.add(Kind.KW_abs);
		f1.add(Kind.KW_red);
		f1.add(Kind.KW_green);
		f1.add(Kind.KW_blue);
		f1.add(Kind.KW_alpha);

		f2.add(Kind.KW_abs);
		f2.add(Kind.KW_sin);
		f2.add(Kind.KW_cos);
		f2.add(Kind.KW_atan);
		f2.add(Kind.KW_log);

		f3.add(Kind.KW_width);
		f3.add(Kind.KW_height);

	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {

		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {

		symbolTable.enterScope();
		for(int i=0;i<block.decsOrStatements.size();i++){
			block.decOrStatement(i).visit(this, arg);
		}
		symbolTable.leaveScope();
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {

		if(symbolTable.hasNameClash(declaration.name)){
			throw new SemanticException(declaration.firstToken,"duplicate name declaration in scope");
		}

		if(declaration.width!=null){
			declaration.width.visit(this,arg);
			if((declaration.width.type== Types.Type.INTEGER && Types.getType(declaration.type)== Types.Type.IMAGE)){
				if(declaration.height!=null ){
					declaration.height.visit(this,arg);
					if((declaration.height.type== Types.Type.INTEGER)){
						symbolTable.insertEntry(declaration);
					}
					else throw new SemanticException(declaration.firstToken,"erroor");
				}
				else throw new SemanticException(declaration.firstToken,"erroor");
			}
			else throw new SemanticException(declaration.firstToken,"erroor2");
		}
		else if(declaration.height==null){
			symbolTable.insertEntry(declaration);
		}
		else throw new SemanticException(declaration.firstToken,"erroor3");
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		statementWrite.sourceDec = symbolTable.lookup(statementWrite.sourceName);
		if(statementWrite.sourceDec==null) throw new SemanticException(statementWrite.firstToken,"write statement");
		statementWrite.destDec = symbolTable.lookup(statementWrite.destName);
		if(statementWrite.destDec==null) throw new SemanticException(statementWrite.firstToken,"write statement");
		if(Types.getType(statementWrite.sourceDec.type) != Types.Type.IMAGE) throw new SemanticException(statementWrite.firstToken,"write statement");
		if(Types.getType(statementWrite.destDec.type) != Types.Type.FILE) throw new SemanticException(statementWrite.firstToken,"write statement");
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		statementInput.dec = symbolTable.lookup(statementInput.destName);
		if(statementInput.dec==null) throw new SemanticException(statementInput.firstToken,"input statement");
		statementInput.e.visit(this,arg);
		if(statementInput.e.type!=Types.Type.INTEGER) throw new SemanticException(statementInput.firstToken,"input statement");
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		pixelSelector.ex.visit(this,arg);
		pixelSelector.ey.visit(this,arg);
		if(pixelSelector.ex.type != pixelSelector.ey.type) throw new SemanticException(pixelSelector.firstToken,"pixel selctor");
		if(pixelSelector.ex.type == Types.Type.INTEGER || pixelSelector.ex.type == Types.Type.FLOAT ) return null;
		else throw new SemanticException(pixelSelector.firstToken,"pixel selctor 2");
		//throw new UnsupportedOperationException();
		//return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.guard.visit(this,arg);
		expressionConditional.trueExpression.visit(this,arg);
		expressionConditional.falseExpression.visit(this,arg);

		if(expressionConditional.guard.type != Types.Type.BOOLEAN || expressionConditional.trueExpression.type != expressionConditional.falseExpression.type)
			throw new SemanticException(expressionConditional.firstToken,"expression conditional");
		expressionConditional.type = expressionConditional.trueExpression.type;

		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this,arg);
		expressionBinary.type = inferredType(expressionBinary.leftExpression,expressionBinary.rightExpression,expressionBinary.op);
		if(expressionBinary.type==null) throw new SemanticException(expressionBinary.firstToken,"binary expression");
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		expressionUnary.type = expressionUnary.expression.type;
		//throw new UnsupportedOperationException();
		return null;
	}


	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type = Types.Type.INTEGER;
		//throw new UnsupportedOperationException();
		return null;
	}


	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {

		expressionBooleanLiteral.type = Types.Type.BOOLEAN;
		//throw new UnsupportedOperationException();
		return null;
	}


	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {

		expressionPredefinedName.type = Types.Type.INTEGER;
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {

		//throw new UnsupportedOperationException();
		expressionFloatLiteral.type = Types.Type.FLOAT;
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
			expressionFunctionAppWithExpressionArg.e.visit(this,arg);
			expressionFunctionAppWithExpressionArg.type = inferredType(expressionFunctionAppWithExpressionArg.function,
					expressionFunctionAppWithExpressionArg.e.type);
			if(expressionFunctionAppWithExpressionArg.type==null)
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"expression func app arg");
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		expressionFunctionAppWithPixel.e0.visit(this,arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		if(((expressionFunctionAppWithPixel.name == Kind.KW_cart_x)
				||(expressionFunctionAppWithPixel.name == Kind.KW_cart_y)
				) && expressionFunctionAppWithPixel.e0.type==Types.Type.FLOAT
				&& expressionFunctionAppWithPixel.e1.type ==Types.Type.FLOAT){
			expressionFunctionAppWithPixel.type = Types.Type.INTEGER;
		}
		else if(((expressionFunctionAppWithPixel.name == Kind.KW_polar_a)
				||(expressionFunctionAppWithPixel.name == Kind.KW_polar_r)
		) && expressionFunctionAppWithPixel.e0.type==Types.Type.INTEGER
				&& expressionFunctionAppWithPixel.e1.type ==Types.Type.INTEGER){
			expressionFunctionAppWithPixel.type = Types.Type.FLOAT;
		}
		else{
			throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"expression function with app pixel");
		}
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		expressionPixelConstructor.alpha.visit(this,arg);
		expressionPixelConstructor.red.visit(this,arg);
		expressionPixelConstructor.green.visit(this,arg);
		expressionPixelConstructor.blue.visit(this,arg);
		if(expressionPixelConstructor.alpha.type!=Types.Type.INTEGER
				|| expressionPixelConstructor.red.type!=Types.Type.INTEGER
				||expressionPixelConstructor.green.type!=Types.Type.INTEGER
				||expressionPixelConstructor.blue.type!=Types.Type.INTEGER)
			throw new SemanticException(expressionPixelConstructor.firstToken,"expression pixel constructor");
		expressionPixelConstructor.type = Types.Type.INTEGER;
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {

		statementAssign.lhs.visit(this,arg);
		statementAssign.e.visit(this,arg);
		if(statementAssign.lhs.type != statementAssign.e.type)
			throw new SemanticException(statementAssign.firstToken,"statement assign");
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		statementShow.e.visit(this,arg);
		if(statementShow.e.type == Types.Type.INTEGER
				||statementShow.e.type == Types.Type.FLOAT
				|| statementShow.e.type == Types.Type.IMAGE
				|| statementShow.e.type == Types.Type.BOOLEAN);
		else throw new SemanticException(statementShow.firstToken,"state show");
		//throw new UnsupportedOperationException();
		return null;
	}

	//TODO
	//TODO pixelSelector.visit
	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {

		expressionPixel.dec = symbolTable.lookup(expressionPixel.name);
		expressionPixel.pixelSelector.visit(this, arg);
		if(expressionPixel.dec != null && Types.getType(expressionPixel.dec.type)== Types.Type.IMAGE );
		else throw new SemanticException(expressionPixel.firstToken,"expression pixel ");
		expressionPixel.type = Types.Type.INTEGER;
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {

		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		if(expressionIdent.dec==null) throw new SemanticException(expressionIdent.firstToken,"expression indent");
		expressionIdent.type = Types.getType(expressionIdent.dec.type);
		//throw new UnsupportedOperationException();
		return null;
	}
	//TODO
	//TODO pixelselector.visit
	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {

		lhsSample.dec = symbolTable.lookup(lhsSample.name);
		lhsSample.pixelSelector.visit(this,arg);
		if(lhsSample.dec!=null && Types.getType(lhsSample.dec.type)==Types.Type.IMAGE){
			lhsSample.type = Types.Type.INTEGER;
		}
		else throw new SemanticException(lhsSample.firstToken,"lhs sample");

		//throw new UnsupportedOperationException();
		return null;
	}
	//TODO
	//TODO pixelselector.visit
	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		lhsPixel.dec = symbolTable.lookup(lhsPixel.name);
		lhsPixel.pixelSelector.visit(this,arg);
		if(lhsPixel.dec!=null && Types.getType(lhsPixel.dec.type)==Types.Type.IMAGE){
			lhsPixel.type = Types.Type.INTEGER;
		}
		else throw new SemanticException(lhsPixel.firstToken,"lhs pixel");

		//throw new UnsupportedOperationException();
		return null;
	}
	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		lhsIdent.dec = symbolTable.lookup(lhsIdent.name);
		if(lhsIdent.dec!=null){
			lhsIdent.type = Types.getType(lhsIdent.dec.type);
		}
		else throw new SemanticException(lhsIdent.firstToken,"lhs ident");

		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {

		//throw new UnsupportedOperationException();
		statementIf.guard.visit(this,arg);
		statementIf.b.visit(this,arg);
		if(statementIf.guard.type != Types.Type.BOOLEAN)
			throw new SemanticException(statementIf.firstToken,"state if");
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {

		//throw new UnsupportedOperationException();
		statementWhile.guard.visit(this,arg);
		statementWhile.b.visit(this, arg);
		if(statementWhile.guard.type != Types.Type.BOOLEAN)
			throw new SemanticException(statementWhile.firstToken,"state while");
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {

		//throw new UnsupportedOperationException();
		statementSleep.duration.visit(this,null);
		if(statementSleep.duration.type != Types.Type.INTEGER)
			throw new SemanticException(statementSleep.firstToken,"state sleep");
		return null;
	}




	public Types.Type inferredType(Expression e1, Expression e2, Kind op){

		if(e1.type==Types.Type.INTEGER && e2.type==Types.Type.INTEGER && (s1.contains(op)||s3.contains(op)||op==Kind.OP_MOD)){
			return Types.Type.INTEGER;
		}
		else if(e1.type==Types.Type.FLOAT && e2.type==Types.Type.FLOAT && (s1.contains(op))){
			return Types.Type.FLOAT;
		}
		else if(e1.type==Types.Type.INTEGER && e2.type==Types.Type.FLOAT && (s1.contains(op))){
			return Types.Type.FLOAT;
		}
		else if(e1.type==Types.Type.FLOAT && e2.type==Types.Type.INTEGER && (s1.contains(op))){
			return Types.Type.FLOAT;
		}
		else if(e1.type==Types.Type.BOOLEAN && e2.type==Types.Type.BOOLEAN && (s3.contains(op))){
			return Types.Type.BOOLEAN;
		}
		else if(e1.type==Types.Type.INTEGER && e2.type==Types.Type.INTEGER && (s3.contains(op))){
			return Types.Type.INTEGER;
		}
		else if(e1.type==Types.Type.INTEGER && e2.type==Types.Type.INTEGER && (s2.contains(op))){
			return Types.Type.BOOLEAN;
		}
		else if(e1.type==Types.Type.FLOAT && e2.type==Types.Type.FLOAT && (s2.contains(op))){
			return Types.Type.BOOLEAN;
		}
		else if(e1.type==Types.Type.BOOLEAN && e2.type==Types.Type.BOOLEAN && (s2.contains(op))){
			return Types.Type.BOOLEAN;
		}
		else return null;

	}

	public Types.Type inferredType(	Kind fName,Types.Type t){
		if(t==Types.Type.INTEGER && (f1.contains(fName))){
			return Types.Type.INTEGER;
		}
		else if(t==Types.Type.FLOAT && (f2.contains(fName))){
			return Types.Type.FLOAT;
		}
		else if(t==Types.Type.IMAGE && (f3.contains(fName))){
			return Types.Type.INTEGER;
		}
		else if(t==Types.Type.INTEGER && fName==Kind.KW_float){
			return Types.Type.FLOAT;
		}
		else if(t==Types.Type.FLOAT && fName==Kind.KW_float){
			return Types.Type.FLOAT;
		}
		else if(t==Types.Type.FLOAT && fName==Kind.KW_int){
			return Types.Type.INTEGER;
		}
		else if(t==Types.Type.INTEGER && fName==Kind.KW_int){
			return Types.Type.INTEGER;
		}
		else return null;

	}

}
