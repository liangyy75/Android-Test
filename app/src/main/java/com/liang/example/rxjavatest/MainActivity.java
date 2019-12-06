package com.liang.example.rxjavatest;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.R;
import com.liang.example.utils.ApiManager;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.processors.AsyncProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RxJava";
    private Disposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_handler);
        // TODO

        test1();
        test2();
        test3();
        test4();
    }

    @SuppressLint("CheckResult")
    private static void test4() {
        // Subject<String> subject = new Subject<String>() {
        //     @Override
        //     public void onSubscribe(Disposable d) {
        //
        //     }
        //
        //     @Override
        //     public void onNext(String s) {
        //
        //     }
        //
        //     @Override
        //     public void onError(Throwable e) {
        //
        //     }
        //
        //     @Override
        //     public void onComplete() {
        //
        //     }
        //
        //     @Override
        //     protected void subscribeActual(Observer<? super String> observer) {
        //
        //     }
        //
        //     @Override
        //     public boolean hasObservers() {
        //         return false;
        //     }
        //
        //     @Override
        //     public boolean hasThrowable() {
        //         return false;
        //     }
        //
        //     @Override
        //     public boolean hasComplete() {
        //         return false;
        //     }
        //
        //     @Override
        //     public Throwable getThrowable() {
        //         return null;
        //     }
        // };
        Observable.create((ObservableOnSubscribe<Void>) emitter -> {
            PublishSubject<Integer> subject = PublishSubject.create();
            subject.subscribe(i -> System.out.print("(1: " + i + ") "));
            @SuppressLint("CI_NotAllowInvokeExecutorsMethods") ExecutorService executor = Executors.newFixedThreadPool(5);
            Disposable disposable = Observable.range(1, 5).subscribe(i -> executor.execute(() -> {
                try {
                    Thread.sleep(i * 200);
                    subject.onNext(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
            Thread.sleep(500);
            subject.subscribe(i -> System.out.print("(2: " + i + ") "));
            Observable.timer(2, TimeUnit.SECONDS).subscribe(i -> executor.shutdown());

            ReplaySubject<Integer> subject2 = ReplaySubject.create();
            subject2.subscribe(i -> System.out.print("(1: " + i + ") "));
            @SuppressLint("CI_NotAllowInvokeExecutorsMethods") ExecutorService executor2 = Executors.newFixedThreadPool(5);
            Disposable disposable2 = Observable.range(1, 5).subscribe(i -> executor2.execute(() -> {
                try {
                    Thread.sleep(i * 200);
                    subject2.onNext(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
            Thread.sleep(500);
            subject2.subscribe(i -> System.out.print("(2: " + i + ") "));
            Observable.timer(2, TimeUnit.SECONDS).subscribe(i -> executor2.shutdown());
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }

    // Flowable / Subscriber / FlowableSubscriber
    private void test2() {
        Flowable<String> flowable = Flowable.create(emitter -> {
            emitter.onNext("1");
            emitter.onNext("2");
            emitter.onNext("3");
        }, BackpressureStrategy.ERROR);
        flowable.subscribe(
                new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(3);
                        ApiManager.LOGGER.d(TAG, "test2 -- Subscriber -- onSubscribe");
                    }

                    @Override
                    public void onNext(String s) {
                        ApiManager.LOGGER.d(TAG, "test2 -- Subscriber -- onNext " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        ApiManager.LOGGER.d(TAG, "test2 -- Subscriber -- onError", e);
                    }

                    @Override
                    public void onComplete() {
                        ApiManager.LOGGER.d(TAG, "test2 -- Subscriber -- onComplete");
                    }
                });
        flowable.subscribe(new FlowableSubscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
                s.request(2);
                s.request(1);
                ApiManager.LOGGER.d(TAG, "test2 -- FlowableSubscriber -- onSubscribe");
            }

            @Override
            public void onNext(String s) {
                ApiManager.LOGGER.d(TAG, "test2 -- FlowableSubscriber -- onNext " + s);
            }

            @Override
            public void onError(Throwable e) {
                ApiManager.LOGGER.d(TAG, "test2 -- FlowableSubscriber -- onError", e);
            }

            @Override
            public void onComplete() {
                ApiManager.LOGGER.d(TAG, "test2 -- FlowableSubscriber -- onComplete");
            }
        });
    }

    // Observable / Consumer / Observer
    private void test3() {
        Observable<Integer> observable = new Observable<Integer>() {
            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onNext(1);
                observer.onNext(2);
                observer.onNext(3);
            }
        };
        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                ApiManager.LOGGER.d(TAG, "test3 -- Observer -- onSubscribe");
            }

            @Override
            public void onNext(Integer integer) {
                ApiManager.LOGGER.d(TAG, "test3 -- Observer -- onNext " + integer);
            }

            @Override
            public void onError(Throwable e) {
                ApiManager.LOGGER.d(TAG, "test3 -- Observer -- onError", e);
            }

            @Override
            public void onComplete() {
                ApiManager.LOGGER.d(TAG, "test3 -- Observer -- onComplete");
            }
        });
        Disposable disposable = observable.subscribe(integer -> ApiManager.LOGGER.d(TAG, "test3 -- Consumer -- accept: " + integer));
    }

    // basic test
    private void test1() {
        RxJavaPlugins.setErrorHandler(throwable -> ApiManager.LOGGER.w(TAG, "test1 -- errorHandler", throwable)); // 有了这句，onError不会在dispose之后报错了，也不会在
        Observable<String> observable = Observable.create((ObservableEmitter<String> emitter) -> {
            emitter.setCancellable(() -> ApiManager.LOGGER.d(TAG, "test1 -- do cancel action!!!"));
            final boolean[] flag = {true};
            emitter.setDisposable(new Disposable() {
                @Override
                public void dispose() {
                    flag[0] = false;
                }

                @Override
                public boolean isDisposed() {
                    return flag[0];
                }
            });
            int i = 0;
            while (flag[0]) {
                i++;
                emitter.onNext(String.valueOf(i));
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    ApiManager.LOGGER.w(TAG, "test1 -- observable on " + Thread.currentThread().getName(), e);
                    emitter.onError(e);
                }
                ApiManager.LOGGER.d(TAG, "test1 -- observable on " + Thread.currentThread().getName() + ", continue " + i);
            }
        });
        disposable = observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((String s) -> ApiManager.LOGGER.d(TAG, "test1 -- subscribe: " + s + ", on -- " + Thread.currentThread().getName()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}
