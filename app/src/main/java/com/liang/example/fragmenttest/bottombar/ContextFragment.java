package com.liang.example.fragmenttest.bottombar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liang.example.androidtest.R;

public class ContextFragment extends Fragment {
    public final static String TEXT_VIEW_NAME_KEY = "TEXT_VIEW_NAME_KEY";
    private View.OnClickListener okClickListener;
    private View.OnClickListener cancelClickListener;

    public void setCancelClickListener(View.OnClickListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
    }

    public void setOkClickListener(View.OnClickListener okClickListener) {
        this.okClickListener = okClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_fragment_bottombar, container, false);
        if (okClickListener != null) {
            view.findViewById(R.id.test_fragment_bottombar_item_ok).setOnClickListener(okClickListener);
        }
        if (cancelClickListener != null) {
            view.findViewById(R.id.test_fragment_bottombar_item_cancel).setOnClickListener(cancelClickListener);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            TextView textView = view.findViewById(R.id.test_fragment_bottombar_item_name);
            textView.setText(bundle.getString(TEXT_VIEW_NAME_KEY));
        }
        return view;
    }
}
