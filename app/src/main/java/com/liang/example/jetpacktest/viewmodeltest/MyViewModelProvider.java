package com.liang.example.jetpacktest.viewmodeltest;

import androidx.annotation.NonNull;
import androidx.lifecycle.HasDefaultViewModelProviderFactory;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.liang.example.utils.ApiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// TODO: test
public class MyViewModelProvider extends ViewModelProvider {
    private static final String DEFAULT_KEY =
            "androidx.lifecycle.ViewModelProvider.DefaultKey";
    private final Factory mFactory;
    private final ViewModelStore mViewModelStore;
    private static Method getMethod, putMethod;

    static {
        for (int i = 0; i < 3; i++) {
            try {
                getMethod = ViewModelStore.class.getDeclaredMethod("get", String.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Impossible occurred exception in MyViewModelProvider about getMethod", e);
            }
            try {
                putMethod = ViewModelStore.class.getDeclaredMethod("put", String.class, ViewModel.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Impossible occurred exception in MyViewModelProvider about putMethod", e);
            }
        }
        getMethod.setAccessible(true);
        putMethod.setAccessible(true);
    }

    public abstract static class KeyedFactory implements ViewModelProvider.Factory {
        /**
         * Creates a new instance of the given {@code Class}.
         *
         * @param key        a key associated with the requested ViewModel
         * @param modelClass a {@code Class} whose instance is requested
         * @param <T>        The type parameter for the ViewModel.
         * @return a newly created ViewModel
         */
        @NonNull
        public abstract <T extends ViewModel> T create(@NonNull String key,
                                                       @NonNull Class<T> modelClass);

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            throw new UnsupportedOperationException("create(String, Class<?>) must be called on "
                    + "implementaions of KeyedFactory");
        }
    }

    public MyViewModelProvider(@NonNull ViewModelStoreOwner owner) {
        this(owner.getViewModelStore(), owner instanceof HasDefaultViewModelProviderFactory
                ? ((HasDefaultViewModelProviderFactory) owner).getDefaultViewModelProviderFactory()
                : DefaultMyFactory.getInstance());
    }

    public MyViewModelProvider(@NonNull ViewModelStoreOwner owner, @NonNull Factory factory) {
        this(owner.getViewModelStore(), factory);
    }

    public MyViewModelProvider(@NonNull ViewModelStore store, @NonNull Factory factory) {
        super(store, factory);
        mFactory = factory;
        mViewModelStore = store;
    }

    public <T extends ViewModel> T get(@NonNull Class<T> modelClass, Object... args) {
        String canonicalName = modelClass.getCanonicalName();
        if (canonicalName == null) {
            throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
        }
        return get(DEFAULT_KEY + ":" + canonicalName, modelClass, args);
    }

    public <T extends ViewModel> T get(String key, @NonNull Class<T> modelClass, Object... args) {
        // ViewModel viewModel = mViewModelStore.get(key);
        ViewModel viewModel = null;
        try {
            viewModel = (ViewModel) getMethod.invoke(mViewModelStore, key);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Impossible occurred IllegalAccessException in MyViewModelProvider about getMethod", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Impossible occurred InvocationTargetException in MyViewModelProvider about getMethod", e);
        }

        if (modelClass.isInstance(viewModel)) {
            return (T) viewModel;
        } else {
            //noinspection StatementWithEmptyBody
            if (viewModel != null) {
                ApiManager.LOGGER.w("MyViewModelProvider", "viewModel == null, and it's impossible!!!");
            }
        }
        if (mFactory instanceof DefaultMyFactory) {
            viewModel = ((DefaultMyFactory) mFactory).create(modelClass, args);
        } else if (mFactory instanceof MyFactory) {
            viewModel = ((MyFactory) mFactory).create(key, modelClass, args);
        } else if (mFactory instanceof KeyedFactory) {
            viewModel = ((KeyedFactory) (mFactory)).create(key, modelClass);
        } else {
            viewModel = (mFactory).create(modelClass);
        }

        // mViewModelStore.put(key, viewModel);
        try {
            putMethod.invoke(mViewModelStore, key, viewModel);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Impossible occurred IllegalAccessException in MyViewModelProvider about putMethod", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Impossible occurred InvocationTargetException in MyViewModelProvider about putMethod", e);
        }
        return (T) viewModel;
    }

    public abstract class MyFactory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            throw new UnsupportedOperationException("create(Class<?>, Object...) must be called on "
                    + "implementaions of MyFactory");
        }

        @NonNull
        public abstract <T extends ViewModel> T create(String key, @NonNull Class<T> modelClass, Object... args);
    }

    public static class DefaultMyFactory extends ViewModelProvider.NewInstanceFactory {
        private DefaultMyFactory() {
        }

        private static DefaultMyFactory instance;

        public static DefaultMyFactory getInstance() {
            if (instance == null) {
                instance = new DefaultMyFactory();
            }
            return instance;
        }

        public <T extends ViewModel> T create(@NonNull Class<T> modelClass, Object... args) {
            if (args.length > 0) {
                Class<?>[] parameterTypes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    parameterTypes[i] = args[i].getClass();
                }
                try {
                    return modelClass.getConstructor(parameterTypes).newInstance(args);
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            return super.create(modelClass);
        }
    }
}
