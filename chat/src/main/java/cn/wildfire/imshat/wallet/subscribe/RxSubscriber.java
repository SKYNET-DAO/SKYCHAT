package cn.wildfire.imshat.wallet.subscribe;


public  abstract class RxSubscriber<T> extends ErrorSubscriber<T> {


    @Override
    public void onComplete() {
        //TODO 
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO 

    }

    
    @Override
    public void onNext(T t) {

          onSuccess(t);
    }
    public abstract  void onSuccess(T t);

}