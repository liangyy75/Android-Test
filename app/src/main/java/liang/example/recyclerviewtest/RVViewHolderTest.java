package liang.example.recyclerviewtest;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RVViewHolderTest extends RecyclerView.ViewHolder {
    private View root;
    private SparseArray<View> views;

    public RVViewHolderTest(@NonNull View root) {
        super(root);
        this.root = root;
        this.views = new SparseArray<>();
    }

    public static RVViewHolderTest get(Context context, ViewGroup parent, int layoutId) {
        return new RVViewHolderTest(LayoutInflater.from(context).inflate(layoutId, parent, false));
    }

    public View getRoot() {
        return root;
    }

    public <T extends View> T getViewById(int id) {
        View view = views.get(id);
        if (view == null) {
            view = root.findViewById(id);
            views.put(id, view);
        }
        return (T) view;
    }
}
