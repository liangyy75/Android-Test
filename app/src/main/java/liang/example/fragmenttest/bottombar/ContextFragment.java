package liang.example.fragmenttest.bottombar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import liang.example.androidtest.R;

public class ContextFragment extends Fragment {
    public final static String TEXTVIEW_NAME_KEY = "TEXTVIEW_NAME_KEY";
    public final static String BUTTON_OK_KEY = "BUTTON_OK_KEY";
    public final static String BUTTON_CANCEL_KEY = "BUTTON_CANCEL_KEY";

    TextView textView;
    Button ok, cancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_fragment_bottombar, container, false);
        textView = view.findViewById(R.id.test_fragment_bottombar_item_name);
        ok = view.findViewById(R.id.test_fragment_bottombar_item_ok);
        cancel = view.findViewById(R.id.test_fragment_bottombar_item_cancel);
        Bundle bundle = getArguments();
        if (bundle != null) {
            textView.setText(bundle.getString(TEXTVIEW_NAME_KEY));
        }
        return view;
    }

    public void setButtonListener(String key, View.OnClickListener listener) {
        switch (key) {
            case BUTTON_OK_KEY : ok.setOnClickListener(listener); break;
            case BUTTON_CANCEL_KEY : cancel.setOnClickListener(listener); break;
        }
    }
}
