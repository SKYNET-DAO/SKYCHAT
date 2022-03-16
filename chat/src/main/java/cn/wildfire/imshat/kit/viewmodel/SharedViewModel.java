package cn.wildfire.imshat.kit.viewmodel;

import java.util.concurrent.atomic.AtomicInteger;

import androidx.lifecycle.ViewModel;

public abstract class SharedViewModel extends ViewModel {

    private AtomicInteger mRefCounter;

    private Runnable mOnShareCleared;

    protected SharedViewModel() {
        // do nothing
    }

    protected SharedViewModel(Runnable onShareCleared) {
        this();
        mRefCounter = new AtomicInteger(0);
        mOnShareCleared = onShareCleared;
    }

    
    @Override
    protected final void onCleared() {
        decRefCount();
    }

    
    protected abstract void onShareCleared();

    protected final int incRefCount() {
        return mRefCounter.incrementAndGet();
    }

    private final int decRefCount() {
        int counter = mRefCounter.decrementAndGet();
        if (counter == 0) {
            if (mOnShareCleared != null) {
                mOnShareCleared.run();
            }
            onShareCleared();
        } else if (counter < 0) {
            mRefCounter.set(0);
            counter = 0;
        }
        return counter;
    }
}
