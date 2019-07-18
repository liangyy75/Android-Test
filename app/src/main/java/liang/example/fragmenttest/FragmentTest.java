package liang.example.fragmenttest;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 1. Created
 * 1. onAttach: fragment与窗口关联后立刻调用，从这里开始可以调用fragment.getActivity
 * 2. onCreate: onAttach后立即调用，可以在Bundle对象中获取一些在Activity中传过来的数据(不执行耗时操作，不然窗口不显示)
 * 3. onCreateView: 创建view
 * 4. onViewCreated: 创建view后立即调用，它之后Activity才会调用onCreate
 * 5. onActivityCreated: 在Activity的onCreate调用后调用，从这一个时候开始，就可以在Fragment中使用getActivity().findViewById(Id);来操控Activity中的view了。
 * 2. Started: onStart
 * 3. Resumed: onResume
 * 4. Paused: onPause: onPause后的方法都在Activity相应的方法前调用
 * 5. Stopped: onStop
 * 6. Destroy:
 * 1. onDestroyView
 * 2. onDestroy
 * 3. onDetach: 它之后才是Activity的onDestroy
 * 7. **注意**: 除了onCreateView，其他的所有方法如果你重写了，必须调用父类对于该方法的实现
 */
public class FragmentTest extends Fragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
