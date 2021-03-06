package expressions;

import java.util.Set;

import context.Context;
import expressions.exceptions.ContextIncompleteException;
import expressions.exceptions.DivByZeroException;
import values.Value;

/**
 * Wrapper für Ausdrücke, dessen equals auf Referenzgleichheit testet. Wird genutzt, um einen
 * Ausdrucksbaum auf Zyklenfreiheit zu überprüfen.
 * 
 * @author kar, mhe, Lars Sander, Alexander Loeffler
 * @param <V> extends Value Der Ausdrucksbaum arbeitet mit eigens definierten Typen, daher werden
 *            nur Unterarten von Value erlaubt
 */
public class ExpressionWrapper<V extends Value<V>> extends AbstractExpression<V> {

    /**
     * Der eingewickelte Ausdruck.
     */
    protected final Expression<V> subExpression;

    /**
     * Konstruktor.
     * 
     * @param subExpression Der einzuwickelnde Ausdruck
     */
    public ExpressionWrapper(Expression<V> subExpression) {
        this.subExpression = subExpression;
    }

    @Override
    public StringBuilder toString(StringBuilder builder) {
        return this.subExpression.toString(builder);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ExpressionWrapper) {
            return this.subExpression == ((ExpressionWrapper<?>) other).subExpression;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this.subExpression);
    }

    @Override
    public V evaluate(Context<V> c) throws ContextIncompleteException, DivByZeroException {
        return subExpression.evaluate(c);
    }

    @Override
    public boolean isConst() {
        return subExpression.isConst();
    }

    @Override
    public boolean hasCycles() {
        return subExpression.hasCycles();
    }

    @Override
    public boolean checkCycle(Set<ExpressionWrapper<?>> checked) {
        return subExpression.checkCycle(checked);
    }

}
