package nsu.fit.g14201.marchenko.phoenix.ui.fragments;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import nsu.fit.g14201.marchenko.phoenix.ui.activities.BaseActivity;

public abstract class BaseFragment extends Fragment {
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    protected abstract int getLayoutId();

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    protected void showToast(String message) {
        getBaseActivity().showToast(message);
    }

    protected void showSnack(String message) {
        getBaseActivity().showSnack(message);
    }

    protected void hideKeyboard(View view) {
        getBaseActivity().hideKeyboard(view);
    }
}
