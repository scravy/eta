package eta.runtime.apply;

import eta.runtime.stg.StgClosure;
import eta.runtime.stg.StgContext;
import eta.runtime.stg.StackFrame;

public class ApO extends StackFrame {
    public Object o;

    public ApO(Object o) {
        this.o = o;
    }

    @Override
    public void stackEnter(StgContext context) {
        context.R(1).applyO(context, o);
    }
}
