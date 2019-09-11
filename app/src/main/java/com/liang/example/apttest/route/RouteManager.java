package com.liang.example.apttest.route;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;

public class RouteManager {
    private static volatile RouteManager instance;
    private Route$Finder finder;

    private RouteManager() {
        finder = new Route$Finder();
    }

    public static RouteManager getInstance() {
        if (instance == null) {
            synchronized (RouteManager.class) {
                if (instance == null) {
                    instance = new RouteManager();
                }
            }
        }
        return instance;
    }

    public boolean navigate(Activity activity, String path) {
        Class clazz = finder.getActivityName(path);
        if (clazz != null) {
            activity.startActivity(new Intent(activity, clazz));
            return true;
        }
        return false;
    }

    public NavigateItem build(Activity activity) {
        return new NavigateItem(activity);
    }

    public class NavigateItem {
        private Activity activity;
        private Intent intent;

        NavigateItem(Activity activity) {
            this.activity = activity;
        }

        public NavigateItem makeIntent() {
            intent = new Intent();
            return this;
        }

        public NavigateItem setIntent(Intent intent) {
            this.intent = intent;
            return this;
        }

        public NavigateItem putExtras(Intent src) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtras(src);
            return this;
        }

        public NavigateItem putExtra(String key, boolean... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtra(String key, char... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtra(String key, CharSequence... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtra(String key, byte... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtra(String key, short... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtra(String key, int... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtra(String key, long... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtra(String key, float... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtra(String key, double... value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public final <T extends Parcelable> NavigateItem putExtra(String key, T value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public final <T extends Parcelable> NavigateItem putExtra(String key, T[] value) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtrasUnSafe(Intent src) {
            intent.putExtras(src);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, boolean... value) {
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, char... value) {
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, CharSequence... value) {
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, byte... value) {
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, short... value) {
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, int... value) {
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, long... value) {
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, float... value) {
            intent.putExtra(key, value);
            return this;
        }

        public NavigateItem putExtraUnSafe(String key, double... value) {
            intent.putExtra(key, value);
            return this;
        }

        public <T extends Parcelable> NavigateItem putExtraUnSafe(String key, T value) {
            intent.putExtra(key, value);
            return this;
        }

        public <T extends Parcelable> NavigateItem putExtraUnSafe(String key, T[] value) {
            intent.putExtra(key, value);
            return this;
        }

        public boolean navigate(String path) {
            Class clazz = finder.getActivityName(path);
            if (clazz != null) {
                if (intent == null) {
                    intent = new Intent();
                }
                intent.setClass(activity, clazz);
                activity.startActivity(intent);
                return true;
            }
            return false;
        }
    }
}
