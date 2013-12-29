package com.easyinsight.calculations;

import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.core.Value;

import java.util.Set;
import java.util.HashSet;

/**
 * User: Alan
 * Date: Aug 3, 2008
 * Time: 10:14:02 AM
 */
public class VariableListVisitor implements ICalculationTreeVisitor {
    private Set<AnalysisItem> variableList = new HashSet<AnalysisItem>();

    public void visit(CalculationTreeNode node) {
        visitChildren(node);
    }

    public void visit(NumberNode node) {
        visitChildren(node);
    }

    public void visit(StringNode node) {
        visitChildren(node);
    }

    public void visit(EqualsNode node) {
        visitChildren(node);
    }

    public void visit(NotEqualsNode node) {
        visitChildren(node);
    }

    public void visit(GreaterThanNode node) {
        visitChildren(node);
    }

    public void visit(GreaterThanEqualToNode node) {
        visitChildren(node);
    }

    public void visit(LessThanNode node) {
        visitChildren(node);
    }

    public void visit(LessThanEqualToNode node) {
        visitChildren(node);
    }

    public void visit(AndNode node) {
        visitChildren(node);
    }

    public void visit(OrNode node) {
        visitChildren(node);
    }

    public void visit(NotNode node) {
        visitChildren(node);
    }

    public void visit(AddNode node) {
        visitChildren(node);
    }

    public void visit(SubtractNode node) {
        visitChildren(node);
    }

    public void visit(MultiplyNode node) {
        visitChildren(node);
    }

    public void visit(DivideNode node) {
        visitChildren(node);
    }

    public void visit(ExponentNode node) {
        visitChildren(node);
    }

    public void visit(VariableNode node) {
        variableList.add(node.getAnalysisItem());
    }

    public void visit(FunctionNode node) {
        visitChildren(node, 1);
    }

    public Value getResult() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
    public Set<AnalysisItem> getVariableList() {
        return variableList;
    }

    private void visitChildren(CalculationTreeNode node) {
        visitChildren(node, 0);
    }

    private void visitChildren(CalculationTreeNode node, int start) {

        for(int i = start;i < node.getChildCount();i++) {
            VariableListVisitor v = new VariableListVisitor();
            if (node.getChild(i) instanceof CalculationTreeNode) {
                ((CalculationTreeNode) node.getChild(i)).accept(v);
            }
            variableList.addAll(v.getVariableList());
        }
    }
}
