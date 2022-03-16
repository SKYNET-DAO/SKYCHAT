package cn.wildfire.imshat.net.helper.rxjavahelper;

import cn.wildfire.imshat.net.model.base.bean.ResponseData;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;



public class RxResultHelper {

    private static final int RESPONSE_SUCCESS_CODE = 0; 
    private static final int RESPONSE_ERROR_CODE = -1;



    public static <T> ObservableTransformer<ResponseData<T>, T> handleResult() {
        return new ObservableTransformer<ResponseData<T>, T>() {
            @Override
            public ObservableSource<T> apply(Observable<ResponseData<T>> tObservable) {
                return tObservable.flatMap(
                        new Function<ResponseData<T>, Observable<T>>() {
                            @Override
                            public Observable<T> apply(ResponseData<T> tResponseData) throws Exception {
                                
                                if (tResponseData.getCode() == RESPONSE_SUCCESS_CODE) {
                                    return Observable.just(tResponseData.getData());
                                } else if (tResponseData.getCode() == RESPONSE_ERROR_CODE) {
                                    return Observable.error(new Exception(tResponseData.getCode()+""));
                                } else {
                                    return Observable.empty();
                                }
                            }
                        }
                );
            }

        };
    }

}
