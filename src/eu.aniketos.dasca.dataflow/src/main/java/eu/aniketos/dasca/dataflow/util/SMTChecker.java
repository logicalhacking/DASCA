/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.wala.cast.ir.ssa.AstConstants;
import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
import com.ibm.wala.shrikeBT.BinaryOpInstruction;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction.IOperator;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;

import cvc3.Expr;
import cvc3.ValidityChecker;

public class SMTChecker {

    private static Logger log = AnalysisUtil.getLogger(SMTChecker.class);

    public static Expr getExprForCondition(ValidityChecker vc, BasicBlock basicBlock, IR entryIR) {
        // According to WALA documentation these blocks contain exactly one instruction
        SSAConditionalBranchInstruction inst = (SSAConditionalBranchInstruction) basicBlock.getLastInstruction();
        int var = inst.getUse(0); // This Instruction contains exactly two uses (0, 1) and use 1 is always 0.
        Expr expr = getExprForInstruction(vc, var, entryIR, null);

        return expr;
    }

    /**
     * Returns simple expression for loop-condition. Expects for-loop with simple comparison with constant on the right hand side
     *
     * <br />TODO: implement correct for all kinds of loops
     * @param vc
     * @param inst
     * @param entryIR
     * @return
     */
    public static Expr getExprForLoop(ValidityChecker vc, SSAInstruction inst, IR entryIR) {
        // According to WALA documentation these blocks contain exactly one instruction
        SSAConditionalBranchInstruction condInst = (SSAConditionalBranchInstruction) inst;
        int var = condInst.getUse(0); // This Instruction contains exactly the two uses 0 and 1. Use 1 has always the value 0, since it stands for TRUE.
        SymbolTable symTab = entryIR.getSymbolTable();
        DefUse du = new DefUse(entryIR);

        SSAInstruction useInst = du.getDef(var);
        SSABinaryOpInstruction binOpInstruction = (SSABinaryOpInstruction) useInst;

        int varNo0 = useInst.getUse(0); // value at the left hand side

        Expr exprLhs = null;
        SSAInstruction leftInstruction = du.getDef(varNo0);
        SSAPhiInstruction phiInstruction = (SSAPhiInstruction) leftInstruction;
        for(int i = 0; i< phiInstruction.getNumberOfUses(); i++) { // TODO: use bit array to identify overflows
            int varNo = phiInstruction.getUse(i);
            if(symTab.isIntegerConstant(varNo)) {
                ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                Integer integer = (Integer) value.getValue();
                exprLhs = vc.exprFromString("" + integer.intValue());
                break;
            } else if(symTab.isLongConstant(varNo)) {
                ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                Long longValue = (Long) value.getValue();
                exprLhs = vc.exprFromString("" + longValue.longValue());
                break;
            } else if(symTab.isDoubleConstant(varNo)) {
                ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                Double doubleValue = (Double) value.getValue();
                exprLhs = vc.exprFromString("" + doubleValue.doubleValue());
                break;
            } else if(symTab.isFloatConstant(varNo)) {
                ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                Float floatValue = (Float) value.getValue();
                exprLhs = vc.exprFromString("" + floatValue.floatValue());
                break;
            }
        }

        int varNo1 = useInst.getUse(1); // value at the right hand side

        Expr exprRhs;
        if(symTab.isIntegerConstant(varNo1)) { // TODO: use bit array to identify overflows
            ConstantValue value = (ConstantValue) symTab.getValue(varNo1);
            Integer integer = (Integer) value.getValue();
            exprRhs = vc.exprFromString("" + integer.intValue());
        } else if(symTab.isLongConstant(varNo1)) {
            ConstantValue value = (ConstantValue) symTab.getValue(varNo1);
            Long longValue = (Long) value.getValue();
            exprRhs = vc.exprFromString("" + longValue.longValue());
        } else if(symTab.isDoubleConstant(varNo1)) {
            ConstantValue value = (ConstantValue) symTab.getValue(varNo1);
            Double doubleValue = (Double) value.getValue();
            exprRhs = vc.exprFromString("" + doubleValue.doubleValue());
        } else if(symTab.isFloatConstant(varNo1)) {
            ConstantValue value = (ConstantValue) symTab.getValue(varNo1);
            Float floatValue = (Float) value.getValue();
            exprRhs = vc.exprFromString("" + floatValue.floatValue());
        } else {
            exprRhs = vc.varExpr(String.format("v%d",varNo1), vc.realType());
        }

        IOperator op = binOpInstruction.getOperator();

        if(op == BinaryOpInstruction.Operator.OR) {
            return vc.orExpr(exprLhs, exprRhs);
        } else if(op == BinaryOpInstruction.Operator.AND) {
            return vc.andExpr(exprLhs, exprRhs);
        } else if(op == BinaryOpInstruction.Operator.SUB) {
            return vc.minusExpr(exprLhs, exprRhs);
        } else if(op == BinaryOpInstruction.Operator.ADD) {
            return vc.plusExpr(exprLhs, exprRhs);
        } else if(op == BinaryOpInstruction.Operator.DIV) {
            return vc.divideExpr(exprLhs, exprRhs);
        } else if(op == BinaryOpInstruction.Operator.MUL) {
            return vc.multExpr(exprLhs, exprRhs);
        } else if(op == AstConstants.BinaryOp.LT) {
            return vc.ltExpr(exprLhs, exprRhs);
        } else if(op == AstConstants.BinaryOp.LE) {
            return vc.leExpr(exprLhs, exprRhs);
        } else if(op == AstConstants.BinaryOp.EQ) {
            return vc.eqExpr(exprLhs, exprRhs);
        } else if(op == AstConstants.BinaryOp.GE) {
            return vc.geExpr(exprLhs, exprRhs);
        } else if(op == AstConstants.BinaryOp.GT) {
            return vc.gtExpr(exprLhs, exprRhs);
        } else {
            return vc.notExpr(vc.eqExpr(exprLhs, exprRhs));
        }
    }

    public static Expr getExprForConditionalBranchInstruction(ValidityChecker vc, SSAInstruction inst, IR entryIR) {
        // According to WALA documentation these blocks contain exactly one instruction
        SSAConditionalBranchInstruction condInst = (SSAConditionalBranchInstruction) inst;
        int var = condInst.getUse(0); // This Instruction contains exactly two uses (0, 1) and use 1 is always 0.
        Expr expr = getExprForInstruction(vc, var, entryIR, BinaryOpInstruction.Operator.AND); // The AND operator identifies the expression as a boolean type

        return expr;
    }

    private static Expr getExprForInstruction(ValidityChecker vc, int varNo, IR ir, IOperator op2) {
        SymbolTable symTab = ir.getSymbolTable();
        if(symTab.isConstant(varNo)) {
            if(symTab.isBooleanConstant(varNo)) {
                ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                boolean isTrueConstant = value.isTrueConstant();
                if(symTab.isTrue(varNo)) {
                    return vc.trueExpr();
                } else if(symTab.isFalse(varNo)) {
                    return vc.falseExpr();
                }
                return vc.varExpr(String.format("v%d",varNo), vc.boolType(), (isTrueConstant) ? vc.trueExpr() : vc.falseExpr());
            } else if(symTab.isNumberConstant(varNo)) { // TODO: use bit array to identify overflows
                if(symTab.isIntegerConstant(varNo)) {
                    ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                    Integer integer = (Integer) value.getValue();
                    return vc.exprFromString("" + integer.intValue());
                } else if(symTab.isLongConstant(varNo)) {
                    ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                    Long longValue = (Long) value.getValue();
                    return vc.exprFromString("" + longValue.longValue());
                } else if(symTab.isDoubleConstant(varNo)) {
                    ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                    Double doubleValue = (Double) value.getValue();
                    return vc.exprFromString("" + doubleValue.doubleValue());
                } else if(symTab.isFloatConstant(varNo)) {
                    ConstantValue value = (ConstantValue) symTab.getValue(varNo);
                    Float floatValue = (Float) value.getValue();
                    return vc.exprFromString("" + floatValue.floatValue());
                }
            } else if(symTab.isTrue(varNo)) {
                return vc.trueExpr();
            } else if(symTab.isFalse(varNo)) {
                return vc.falseExpr();
            } else if(symTab.isNullConstant(varNo)) {
                return vc.nullExpr();
            }
        } else {
            DefUse du = new DefUse(ir);
            SSAInstruction defInst = du.getDef(varNo);
            if(defInst == null) {
                if(op2 == BinaryOpInstruction.Operator.OR || op2 == BinaryOpInstruction.Operator.AND) {
                    return vc.varExpr(String.format("v%d",varNo), vc.boolType());
                } else {
                    return vc.varExpr(String.format("v%d",varNo), vc.realType());
                }
            } else if(defInst instanceof SSAGetInstruction) { // def refers to static value
                SSAGetInstruction inst = (SSAGetInstruction) defInst;
                FieldReference fr = inst.getDeclaredField();
                TypeReference tr = fr.getFieldType();
                if(tr.equals(TypeReference.Boolean)) {
                    return vc.varExpr(String.format("v%d",varNo), vc.boolType());
                }
            }
            if(defInst instanceof SSABinaryOpInstruction) {
                SSABinaryOpInstruction binaryInst = (SSABinaryOpInstruction) defInst;

                IOperator op = binaryInst.getOperator();

                // BinaryInstructions always contain exactly two uses

                // left hand side
                int var1 = binaryInst.getUse(0);
                Expr exprLhs = getExprForInstruction(vc, var1, ir, op);

                // right hand side
                int var2 = binaryInst.getUse(1);
                Expr exprRhs = getExprForInstruction(vc, var2, ir, op);

                if(op == BinaryOpInstruction.Operator.OR) {
                    return vc.orExpr(exprLhs, exprRhs);
                } else if(op == BinaryOpInstruction.Operator.AND) {
                    return vc.andExpr(exprLhs, exprRhs);
                } else if(op == BinaryOpInstruction.Operator.SUB) {
                    return vc.minusExpr(exprLhs, exprRhs);
                } else if(op == BinaryOpInstruction.Operator.ADD) {
                    return vc.plusExpr(exprLhs, exprRhs);
                } else if(op == BinaryOpInstruction.Operator.DIV) {
                    return vc.divideExpr(exprLhs, exprRhs);
                } else if(op == BinaryOpInstruction.Operator.MUL) {
                    return vc.multExpr(exprLhs, exprRhs);
                } else if(op == AstConstants.BinaryOp.LT) {
                    return vc.ltExpr(exprLhs, exprRhs);
                } else if(op == AstConstants.BinaryOp.LE) {
                    return vc.leExpr(exprLhs, exprRhs);
                } else if(op == AstConstants.BinaryOp.EQ) {
                    return vc.eqExpr(exprLhs, exprRhs);
                } else if(op == AstConstants.BinaryOp.GE) {
                    return vc.geExpr(exprLhs, exprRhs);
                } else if(op == AstConstants.BinaryOp.GT) {
                    return vc.gtExpr(exprLhs, exprRhs);
                } else if(op == AstConstants.BinaryOp.NE) {
                    return vc.notExpr(vc.eqExpr(exprLhs, exprRhs));
                }
            } else if(defInst instanceof SSAUnaryOpInstruction) { // WALA uses unary operation ONLY for negation
                SSAUnaryOpInstruction unaryInst = (SSAUnaryOpInstruction) defInst;

                int var = unaryInst.getUse(0);
                Expr expr = getExprForInstruction(vc, var, ir, BinaryOpInstruction.Operator.AND);
                return vc.notExpr(expr);
            } else if(defInst instanceof SSAConversionInstruction) {
                SSAConversionInstruction conversionInst = (SSAConversionInstruction) defInst;

                // ConversionInstructions always contain exactly one use
                int var = conversionInst.getUse(0);
                return getExprForInstruction(vc, var, ir, op2);
            } else if(defInst instanceof SSAPhiInstruction) {
                SSAPhiInstruction phiInst = (SSAPhiInstruction) defInst;
                List<Expr> expr = new ArrayList<Expr>();
                for(int i = 0; i<phiInst.getNumberOfUses(); i++) {
                    expr.add(getExprForInstruction(vc, phiInst.getUse(i), ir, null));
                }
                return vc.orExpr(expr);
            } else if(defInst instanceof AstJavaInvokeInstruction) {
                //TODO: handle invoke for accepted methods
                if(op2 == BinaryOpInstruction.Operator.OR || op2 == BinaryOpInstruction.Operator.AND) {
                    return vc.varExpr(String.format("v%d_invoke",varNo), vc.boolType());
                } else {
                    return vc.varExpr(String.format("v%d_invoke",varNo), vc.realType());
                }
            }
            log.error(defInst.getClass() + " is not handled yet");
        }
        log.error("unhandled type of instruction");
        System.exit(1);
        return null;
    }
}

