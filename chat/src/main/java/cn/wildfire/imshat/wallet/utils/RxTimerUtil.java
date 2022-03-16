package cn.wildfire.imshat.wallet.utils;

import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;



public class RxTimerUtil {

    private static Disposable mDisposable;

    
    public static void timer(long milliseconds,TimeUnit unit, final IRxNext next) {
        Observable.timer(milliseconds, unit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if (next != null) {
                            Logger.e("-----onNext--->"+number);
                            next.doNext(number);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        
                        cancel();
                    }

                    @Override
                    public void onComplete() {
                        
                        cancel();
                    }
                });
    }


    
    public static void interval(long milliseconds, final IRxNext next) {
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if (next != null) {
                            next.doNext(number);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }

                });

    }





   
    public static void cancel() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }


    public interface IRxNext {
        void doNext(long number);
    }

}
