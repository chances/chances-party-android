package com.chancesnow.jedux;

import java.util.ArrayList;
import java.util.List;

public final class Store<A, S> {

    public interface Reducer<A, S> {
        S reduce(A action, S currentState);
    }

    public interface Middleware<A, S> {
        void dispatch(Store<A, S> store, A action, NextDispatcher<A> next);
    }

    public interface NextDispatcher<A> {
        void dispatch(A action);
    }

    private S currentState;

    private final Reducer<A, S> reducer;
    private final List<Runnable> subscribers = new ArrayList<>();

    private final Middleware<A, S> dispatcher = new Middleware<A, S>() {
        @Override
        public void dispatch(Store<A, S> store, A action, NextDispatcher<A> next) {
            synchronized (this) {
                currentState = store.reducer.reduce(action, currentState);
            }
            for (int i = 0; i < subscribers.size(); i++) {
                store.subscribers.get(i).run();
            }
        }
    };

    private final List<NextDispatcher<A>> next = new ArrayList<>();

    public Store(Reducer<A, S> reducer, S state, Middleware<A, S> ...middlewares) {
        this.reducer = reducer;
        this.currentState = state;

        this.next.add(new NextDispatcher<A>() {
            public void dispatch(A action) {
                Store.this.dispatcher.dispatch(Store.this, action, null);
            }
        });
        for (int i = middlewares.length-1; i >= 0; i--) {
            final Middleware<A, S> mw = middlewares[i];
            final NextDispatcher<A> n = next.get(0);
            next.add(0, new NextDispatcher<A>() {
                public void dispatch(A action) {
                    mw.dispatch(Store.this, action, n);
                }
            });
        }
    }

    public S dispatch(A action) {
        this.next.get(0).dispatch(action);
        return this.getState();
    }

    public S getState() {
        return this.currentState;
    }

    public Runnable subscribe(final Runnable r) {
        this.subscribers.add(r);
        return new Runnable() {
            public void run() {
                subscribers.remove(r);
            }
        };
    }
}
