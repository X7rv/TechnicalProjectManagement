package data;

import model.Order;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * UndoManager.java
 * يحفظ آخر العمليات للتراجع عنها (Ctrl+Z).
 * يدعم: ADD, DELETE, UPDATE.
 */
public class UndoManager {

    public enum Action { ADD, DELETE, UPDATE }

    /** عملية واحدة في السجل. */
    public static class Op {
        public final Action action;
        public final Order  order;       // الطلب المتأثر
        public final Order  previousState; // الحالة قبل التعديل (لـ UPDATE فقط)
        public final int    index;       // موضعه في القائمة (لـ DELETE فقط)

        Op(Action action, Order order, Order previousState, int index) {
            this.action = action;
            this.order = order;
            this.previousState = previousState;
            this.index = index;
        }
    }

    private static final int MAX_HISTORY = 50;
    private final Deque<Op> history = new ArrayDeque<>();

    public void recordAdd(Order o) {
        push(new Op(Action.ADD, o, null, -1));
    }

    public void recordDelete(Order o, int index) {
        push(new Op(Action.DELETE, o, null, index));
    }

    public void recordUpdate(Order current, Order previousSnapshot) {
        push(new Op(Action.UPDATE, current, previousSnapshot, -1));
    }

    private void push(Op op) {
        history.push(op);
        while (history.size() > MAX_HISTORY) history.removeLast();
    }

    public boolean canUndo() { return !history.isEmpty(); }

    /** يسحب آخر عملية لاستعادتها. لا يطبّق التراجع - فقط يرجع العملية. */
    public Op pop() {
        return history.isEmpty() ? null : history.pop();
    }

    public void clear() { history.clear(); }

    public int size() { return history.size(); }
}
