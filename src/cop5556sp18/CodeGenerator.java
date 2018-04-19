/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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


package cop5556sp18;

import cop5556sp18.AST.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Types.Type;

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner;

import java.util.ArrayList;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	//new adds
	public int slot = 1;
	ArrayList<Declaration> localVars = new ArrayList<Declaration>();


	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		Label blockStart = new Label();
		Label blockEnd = new Label();

		//visit the start label
		mv.visitLabel(blockStart);
		//then linearly execute
		//all the internals (decs or statements), while
		//passing start and end labels to declarations to
		//take care of the nested blocks scoping and jumps
		for (ASTNode node : block.decsOrStatements) {
			if(node instanceof Declaration){
				Declaration declaration = (Declaration)node;
				declaration.blockStartLabel = blockStart;
				declaration.blockEndLabel = blockEnd;
			}
			node.visit(this, null);
		}
		mv.visitLabel(blockEnd);
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {

		//local var
		declaration.slot = slot; slot++;
		localVars.add(declaration);
		Scanner.Kind decType = declaration.type;
		if(decType == Scanner.Kind.KW_image){

			if(declaration.height!=null &&declaration.width!=null){
				//visit
				declaration.width.visit(this,arg);
				declaration.height.visit(this,arg);
			}
			else{
				//load default values onto stack
				//load constants onto stack
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);
			}
			//gen code to instantiate image, will visit method and put its output onto the top of stack
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className,"makeImage", RuntimeImageSupport.makeImageSig, false);
			//pop the tos and store instantiation to the 'current slot' in ASM's internal array
			mv.visitVarInsn(ASTORE, declaration.slot);
		}
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {

		Expression expression0 = expressionBinary.leftExpression;
		Expression expression1 = expressionBinary.rightExpression;
		Type inferredType = expressionBinary.type;
		Scanner.Kind operator = expressionBinary.op;

		//Labels
		Label wasFalse = new Label();
		Label wasTrue = new Label();
		Label conditionalEndLabel = new Label();


		if(operator == Scanner.Kind.OP_EQ){

			expression0.visit(this, arg);
			expression1.visit(this,arg);

			if(expression0.getType()==Type.BOOLEAN || expression0.getType()==Type.INTEGER ){
				//if not equal, then go to false statement
				mv.visitJumpInsn(IF_ICMPNE, wasFalse);
				//if true--continue and jump to end
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//false label
				mv.visitLabel(wasFalse);
				mv.visitLdcInsn(false);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
			else if(expression0.getType() == Type.FLOAT){

				mv.visitInsn(FCMPG);
				//if not equal, then go to false statement
				mv.visitJumpInsn(IFNE, wasFalse);
				//if true--continue and jump to end
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//false label
				mv.visitLabel(wasFalse);
				mv.visitLdcInsn(false);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
		}else if(operator == Scanner.Kind.OP_NEQ){

			expression0.visit(this, arg);
			expression1.visit(this,arg);

			if(expression0.getType()==Type.BOOLEAN || expression0.getType()==Type.INTEGER ){
				//if not equal, then go to false statement
				mv.visitJumpInsn(IF_ICMPEQ, wasFalse);
				//if true--continue and jump to end
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//false label
				mv.visitLabel(wasFalse);
				mv.visitLdcInsn(false);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
			else if(expression0.getType() == Type.FLOAT){

				mv.visitInsn(FCMPG);
				//if not equal, then go to true statement
				mv.visitJumpInsn(IFNE, wasTrue);
				//if true--continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}

		}else if(operator == Scanner.Kind.OP_GT){

			expression0.visit(this, arg);
			expression1.visit(this,arg);

			if(expression0.getType()==Type.BOOLEAN || expression0.getType()==Type.INTEGER ){
				//if GT goto true
				mv.visitJumpInsn(IF_ICMPGT, wasTrue);
				//if false -- continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
			else if(expression0.getType() == Type.FLOAT){

				mv.visitInsn(FCMPG);
				//if not equal, then go to false statement
				mv.visitJumpInsn(IFGT, wasTrue);
				//if false--continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
		}else if(operator == Scanner.Kind.OP_LT){

			expression0.visit(this, arg);
			expression1.visit(this,arg);

			if(expression0.getType()==Type.BOOLEAN || expression0.getType()==Type.INTEGER ){
				//if GT goto true
				mv.visitJumpInsn(IF_ICMPLT, wasTrue);
				//if false -- continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
			else if(expression0.getType() == Type.FLOAT){

				mv.visitInsn(FCMPG);
				//if not equal, then go to false statement
				mv.visitJumpInsn(IFLT, wasTrue);
				//if false--continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
		}else if(operator == Scanner.Kind.OP_GE){

			expression0.visit(this, arg);
			expression1.visit(this,arg);

			if(expression0.getType()==Type.BOOLEAN || expression0.getType()==Type.INTEGER ){
				//if GT goto true
				mv.visitJumpInsn(IF_ICMPGE, wasTrue);
				//if false -- continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
			else if(expression0.getType() == Type.FLOAT){

				mv.visitInsn(FCMPG);
				//if not equal, then go to false statement
				mv.visitJumpInsn(IFGE, wasTrue);
				//if false--continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
		}else if(operator == Scanner.Kind.OP_LE){

			expression0.visit(this, arg);
			expression1.visit(this,arg);

			if(expression0.getType()==Type.BOOLEAN || expression0.getType()==Type.INTEGER ){
				//if GT goto true
				mv.visitJumpInsn(IF_ICMPLE, wasTrue);
				//if false -- continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
			else if(expression0.getType() == Type.FLOAT){

				mv.visitInsn(FCMPG);
				//if not equal, then go to false statement
				mv.visitJumpInsn(IFLE, wasTrue);
				//if false--continue and jump to end
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, conditionalEndLabel);
				//true label
				mv.visitLabel(wasTrue);
				mv.visitLdcInsn(true);
				//endLabel
				mv.visitLabel(conditionalEndLabel);
			}
		}else if(operator == Scanner.Kind.OP_TIMES){
			//converting everything to float for operation
			//expresion 0
			expression0.visit(this, arg);
			convertUsingOpcode(expression0,Type.INTEGER,I2F);
			//expression 1
			expression1.visit(this,arg);
			convertUsingOpcode(expression1,Type.INTEGER,I2F);
			//multiply
			mv.visitInsn(FMUL);
			//if inferred type was int==> int*int was done
			//so reconverting to int
			if(inferredType==Type.INTEGER){
				mv.visitInsn(F2I);
			}
		}else if(operator == Scanner.Kind.OP_DIV)
		{
			//converting everything to float for operation
			//expresion 0
			expression0.visit(this,arg);
			convertUsingOpcode(expression0,Type.INTEGER,I2F);
			//expression 1
			expression1.visit(this,arg);
			convertUsingOpcode(expression1,Type.INTEGER,I2F);
			//division
			mv.visitInsn(FDIV);
			//if inferred type was int==> int*int was done
			//so reconverting to int
			if(inferredType==Type.INTEGER) {
				mv.visitInsn(F2I);
			}


		}else if(operator == Scanner.Kind.OP_PLUS)
		{
			expression0.visit(this,arg);
			convertUsingOpcode(expression0,Type.INTEGER,I2F);
			expression1.visit(this,arg);
			convertUsingOpcode(expression1,Type.INTEGER,I2F);
			//addition
			mv.visitInsn(FADD);
			if(inferredType==Type.INTEGER) {
				mv.visitInsn(F2I);
			}

		}
		else if(operator == Scanner.Kind.OP_MINUS)
		{
			expression0.visit(this,arg);
			convertUsingOpcode(expression0,Type.INTEGER,I2F);
			expression1.visit(this,arg);
			convertUsingOpcode(expression1,Type.INTEGER,I2F);
			mv.visitInsn(FSUB);
			if(inferredType==Type.INTEGER) {
				mv.visitInsn(F2I);
			}

		}else if(operator == Scanner.Kind.OP_MOD)
		{  	expression0.visit(this,arg);
			expression1.visit(this,arg);
			if(expression0.type==Type.INTEGER && expression1.type==Type.INTEGER) {
				mv.visitInsn(IREM);
			}
			else if(expression0.type==Type.FLOAT && expression1.type==Type.FLOAT) {
				mv.visitInsn(FREM);
			}

		}else if(operator == Scanner.Kind.OP_POWER)
		{
			expression0.visit(this,arg);
			convertUsingOpcode(expression0,Type.INTEGER,I2D);
			convertUsingOpcode(expression0,Type.FLOAT,F2D);
			expression1.visit(this,arg);
			convertUsingOpcode(expression1,Type.INTEGER,I2D);
			convertUsingOpcode(expression1,Type.FLOAT,F2D);
			mv.visitMethodInsn(INVOKESTATIC, Wrapper.className,"power",Wrapper.powerSignature, false);
			if(expression0.type==Type.INTEGER && expression1.type==Type.INTEGER){
				mv.visitInsn(D2I);
			}
			else mv.visitInsn(D2F);

		}else if(operator == Scanner.Kind.OP_AND)
		{
			expression0.visit(this,arg);
			expression1.visit(this,arg);
			mv.visitInsn(IAND);

		}else if(operator == Scanner.Kind.OP_OR)
		{	expression0.visit(this,arg);
			expression1.visit(this,arg);
			mv.visitInsn(IOR);
		}
		return null;
	}

	//helper method
	public void convertUsingOpcode(Expression e, Type type, int opcode){
		if(e.type==type) {
			mv.visitInsn(opcode);
		}
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {

		//init labels
		Label trueExpStartLabel=new Label();
		Label trueExpEndLabel=new Label();

		Label falseExpStartLabel=new Label();
		Label falseExpEndLabel=new Label();

		Label guardStartLabel=new Label();
		Label guardEndLabel=new Label();

		Label endConditionalLabel=new Label();


		mv.visitLabel(guardStartLabel);
		expressionConditional.guard.visit(this,arg);
		mv.visitLabel(guardEndLabel);
		//IFEQ does equals check with zero
		mv.visitJumpInsn(IFEQ,falseExpStartLabel);

		mv.visitLabel(trueExpStartLabel);
		expressionConditional.trueExpression.visit(this,arg);
		mv.visitLabel(trueExpEndLabel);
		mv.visitJumpInsn(GOTO,endConditionalLabel);

		mv.visitLabel(falseExpStartLabel);
		expressionConditional.falseExpression.visit(this,arg);
		mv.visitLabel(falseExpEndLabel);

		mv.visitLabel(endConditionalLabel);

		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		Expression expression = expressionFunctionAppWithExpressionArg.e;
		Scanner.Kind function=expressionFunctionAppWithExpressionArg.function;
		expression.visit(this,null);

		if(function == Scanner.Kind.KW_sin)
		{
			if(expression.type == Type.FLOAT) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "sin", Wrapper.sinSignature, false);
				mv.visitInsn(D2F);
			}

		}else if(function == Scanner.Kind.KW_cos)
		{
			if(expression.type == Type.FLOAT) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "cos", Wrapper.cosSignature, false);
				mv.visitInsn(D2F);
			}

		}else if(function == Scanner.Kind.KW_atan)
		{
			if(expression.type == Type.FLOAT) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "atan", Wrapper.atanSignature, false);
				mv.visitInsn(D2F);
			}

		}else if(function == Scanner.Kind.KW_log)
		{
			if(expression.type == Type.FLOAT) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "log", Wrapper.logSignature, false);
				mv.visitInsn(D2F);
			}

		}else if(function == Scanner.Kind.KW_abs) {

			if(expression.type == Type.INTEGER) {
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "abs", Wrapper.absSignature, false);
				mv.visitInsn(D2I);

			}
			else if(expression.type == Type.FLOAT)
			{	mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "abs", Wrapper.absSignature, false);
				mv.visitInsn(D2F);
			}

		}else if(function == Scanner.Kind.KW_int) {

			if(expression.type == Type.FLOAT)
				mv.visitInsn(F2I);

		} else if(function == Scanner.Kind.KW_float){

			if(expression.type == Type.INTEGER)
				mv.visitInsn(I2F);

		}else if(function == Scanner.Kind.KW_width){

			mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className,"getWidth",RuntimeImageSupport.getWidthSig,false);

		}else if(function == Scanner.Kind.KW_height){

			mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className,"getHeight",RuntimeImageSupport.getHeightSig,false);

		}else if(function == Scanner.Kind.KW_red){

			colorHandler(expression.type,"getRed",RuntimePixelOps.getRedSig);

		}else if(function == Scanner.Kind.KW_blue){

			colorHandler(expression.type,"getBlue",RuntimePixelOps.getBlueSig);

		}else if(function == Scanner.Kind.KW_green){

			colorHandler(expression.type,"getGreen",RuntimePixelOps.getGreenSig);

		}else if(function == Scanner.Kind.KW_alpha){

			colorHandler(expression.type,"getAlpha",RuntimePixelOps.getAlphaSig);
		}


		return null;
	}

	public void colorHandler(Type type, String name,String desc){

		//convert to integer first, then convert back
		if(type==Type.FLOAT) mv.visitInsn(F2I);
		mv.visitMethodInsn(INVOKESTATIC,RuntimePixelOps.className,name,desc);
		if(type==Type.FLOAT) mv.visitInsn(I2F);

	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		Scanner.Kind function = expressionFunctionAppWithPixel.name;
		expressionFunctionAppWithPixel.e0.visit(this,arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		if(function == Scanner.Kind.KW_polar_a || function == Scanner.Kind.KW_polar_r){
			if(function == Scanner.Kind.KW_polar_a) {
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "polara", Wrapper.polaraSignature, false);
			}
			else if(function == Scanner.Kind.KW_polar_r) {
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "polarr", Wrapper.polarrSignature, false);
			}

		}else if(function == Scanner.Kind.KW_cart_x || function == Scanner.Kind.KW_cart_y){
			if(function == Scanner.Kind.KW_cart_x) {
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "cartx", Wrapper.cartxSignature, false);
			}
			else if(function == Scanner.Kind.KW_cart_y) {
				mv.visitMethodInsn(INVOKESTATIC, Wrapper.className, "carty", Wrapper.cartySignature, false);
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		Type type = expressionIdent.type;
		if(type == Type.INTEGER) {
			mv.visitVarInsn(ILOAD,expressionIdent.dec.slot);
		} else if(type == Type.BOOLEAN) {
			mv.visitVarInsn(ILOAD,expressionIdent.dec.slot);
		} else if(type == Type.FLOAT) {
			mv.visitVarInsn(FLOAD,expressionIdent.dec.slot);
		} else if(type == Type.IMAGE) {
			mv.visitVarInsn(ALOAD,expressionIdent.dec.slot);
		} else if(type == Type.FILE) {
			mv.visitVarInsn(ALOAD,expressionIdent.dec.slot);
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		//load the declaration's slot on tos
		mv.visitVarInsn(ALOAD,expressionPixel.dec.slot);
		//visit and take pixel
		expressionPixel.pixelSelector.visit(this,null);
		mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className,"getPixel",RuntimeImageSupport.getPixelSig,false);
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		//don't change order
		expressionPixelConstructor.alpha.visit(this,arg);
		expressionPixelConstructor.red.visit(this,arg);
		expressionPixelConstructor.green.visit(this,arg);
		expressionPixelConstructor.blue.visit(this,arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel",RuntimePixelOps.makePixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {

		if(expressionPredefinedName.name == Scanner.Kind.KW_default_width){
			mv.visitLdcInsn(defaultWidth);
		}else if(expressionPredefinedName.name== Scanner.Kind.KW_default_height){
			mv.visitLdcInsn(defaultHeight);
		}else if(expressionPredefinedName.name== Scanner.Kind.KW_Z){
			mv.visitLdcInsn(Z);
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {

		Expression expression = expressionUnary.expression;
		Type inferredType = expression.type;
		Scanner.Kind operator = expressionUnary.op;

		//visit expression before starting out
		expression.visit(this, arg);
		//negation
		if(operator == Scanner.Kind.OP_MINUS){
			convertUsingOpcode(expression,Type.INTEGER,INEG);
			convertUsingOpcode(expression,Type.FLOAT,FNEG);
		}
		else if(operator == Scanner.Kind.OP_EXCLAMATION){

			if(inferredType == Type.INTEGER){
				//take a temp representation with all bits one
				//then xor our number with this temp, to filp all the bits
				//as -1 ==> 0xFFFFFFFF = 11111111 11111111 11111111 11111111
				mv.visitLdcInsn(new Integer(-1));
				//xoring to flip all bits
				mv.visitInsn(IXOR);
			}else if(inferredType == Type.BOOLEAN){
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IXOR);
			}
		}

		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		Type type = lhsIdent.type;
		if (type == Type.INTEGER) {
			mv.visitVarInsn(ISTORE,lhsIdent.dec.slot);
		}else if (type == Type.BOOLEAN) {
			//booleans internally stored as integer
			mv.visitVarInsn(ISTORE,lhsIdent.dec.slot);
		}
		else if (type == Type.FLOAT) {
			mv.visitVarInsn(FSTORE,lhsIdent.dec.slot);
		}
		else if (type == Type.IMAGE) {
			//value on tos is a reference, do deep copy first
			mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className,"deepCopy",RuntimeImageSupport.deepCopySig,false);
			//then stored the copied reference
			mv.visitVarInsn(ASTORE,lhsIdent.dec.slot);
		} else if(type==Type.FILE) {
			mv.visitVarInsn(ASTORE,lhsIdent.dec.slot);
		}
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, lhsPixel.dec.slot);
		lhsPixel.pixelSelector.visit(this,arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig, false);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, lhsSample.dec.slot);
		lhsSample.pixelSelector.visit(this,arg);
		Scanner.Kind color = lhsSample.color;
		if(color == Scanner.Kind.KW_alpha) {
			mv.visitLdcInsn(RuntimePixelOps.ALPHA);
		}
		if(color == Scanner.Kind.KW_red) {
			mv.visitLdcInsn(RuntimePixelOps.RED);
		}
		if(color == Scanner.Kind.KW_green) {
			mv.visitLdcInsn(RuntimePixelOps.GREEN);
		}
		if(color == Scanner.Kind.KW_blue) {
			mv.visitLdcInsn(RuntimePixelOps.BLUE);
		}
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		pixelSelector.ex.visit(this,arg);
		pixelSelector.ey.visit(this,arg);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);

		//NOTE - BLOCK'S VISIT HAS ALREADY BEEN CALLED EMPLYING THAT THE
		//ARRAYLIST OF VLOCALVARS IS ALREADY COMPLETELY POPULATED
		for(int i=0;i<localVars.size();i++){
			Declaration declaration = localVars.get(i);
			Scanner.Kind type = declaration.type;
			String mapping = null;
			if (type == Scanner.Kind.KW_int) {
				mapping = "I";
			} else if (type == Scanner.Kind.KW_float) {
				mapping = "F";
			} else if (type == Scanner.Kind.KW_boolean) {
				mapping = "Z";
			} else if (type == Scanner.Kind.KW_filename) {
				mapping = "Ljava/lang/String;";
			} else if (type == Scanner.Kind.KW_image){
				mapping = "Ljava/awt/image/BufferedImage;";
			}
			mv.visitLocalVariable(declaration.name,mapping,null,mainStart,mainEnd,declaration.slot);

		}

		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		//generate code to leave expression on tos
		Expression expression = statementAssign.e;
		expression.visit(this,arg);
		//generate code : LHS will pop top, store into itself and leave result on tos
		LHS lhs = statementAssign.lhs;
		lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		Label wasFalse = new Label();
		Label conditionalEnd = new Label();
		statementIf.guard.visit(this,arg);
		//IF QEUAL TO ZERO ==> was false, so jump to end
		mv.visitJumpInsn(IFEQ,conditionalEnd);
		statementIf.b.visit(this,arg);
		mv.visitLabel(conditionalEnd);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Expression expression = statementInput.e;
		//load args[] onto stack
		mv.visitVarInsn(ALOAD,0);
		//puts the index to be used from the array onto stack
		expression.visit(this, arg);
		//pops index, indexes into args,pops args, and loads args[index]
		//on top of stack
		mv.visitInsn(AALOAD);
		Type type = Types.getType(statementInput.dec.type);
		if(type==Type.INTEGER){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitVarInsn(ISTORE,statementInput.dec.slot);

		} else if(type==Type.FLOAT) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
			mv.visitVarInsn(FSTORE,statementInput.dec.slot);

		} else if(type==Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(ISTORE,statementInput.dec.slot);

		} else if(type==Type.FILE) {
			mv.visitVarInsn(ASTORE,statementInput.dec.slot);

		}else if(type==Type.IMAGE){
			Declaration declaration =  statementInput.dec;
			Expression width = declaration.width;
			Expression height= declaration.height;
			if(height == null && width == null){
				//image retains its original size
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			else
			{	//emplies height and width present
				width.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
				height.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
			}
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className,"readImage", RuntimeImageSupport.readImageSig,false);
			mv.visitVarInsn(ASTORE,statementInput.dec.slot);

		}
		return  null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				//change to Z
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);

			}
				break;
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				//changed to F
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
			}
				break;
			case IMAGE : {
				//make frame
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame", RuntimeImageSupport.makeFrameSig,false);
				//pop it off
				mv.visitInsn(POP);

			}

		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		//duration in msecs
		Expression expression = statementSleep.duration;
		expression.visit(this,arg);
		//TODO --- DOES IT SLEEP??? OPCODE 133
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;

	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		Label whileStart = new Label();
		Label whileEnd = new Label();
		mv.visitLabel(whileStart);
		statementWhile.guard.visit(this,arg);
		mv.visitJumpInsn(IFEQ, whileEnd);
		statementWhile.b.visit(this,arg);
		mv.visitJumpInsn(GOTO,whileStart);
		mv.visitLabel(whileEnd);
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {

		Scanner.Kind sourceType = statementWrite.sourceDec.type;
		Scanner.Kind destType = statementWrite.destDec.type;

		if(sourceType == Scanner.Kind.KW_int) {
			mv.visitVarInsn(ILOAD,statementWrite.sourceDec.slot);
		}
		else if(sourceType == Scanner.Kind.KW_float) {
			mv.visitVarInsn(FLOAD,statementWrite.sourceDec.slot);
		}
		else if(sourceType == Scanner.Kind.KW_filename || sourceType == Scanner.Kind.KW_image) {
			mv.visitVarInsn(ALOAD, statementWrite.sourceDec.slot);
		}

		if(destType == Scanner.Kind.KW_int) {
			mv.visitVarInsn(ILOAD,statementWrite.destDec.slot);
		}
		else if(destType == Scanner.Kind.KW_float) {
			mv.visitVarInsn(FLOAD,statementWrite.destDec.slot);
		}
		else if(destType == Scanner.Kind.KW_filename || destType == Scanner.Kind.KW_image) {
			mv.visitVarInsn(ALOAD, statementWrite.destDec.slot);
		}
		mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className, "write", RuntimeImageSupport.writeSig);
		return null;
	}

}
