package nsu.fit.g14201.marchenko.phoenix.registration;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;

import butterknife.OnClick;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.fragment.BaseFragment;

public class AuthorizationFragment extends BaseFragment implements
        AuthorizationContract.View {
    private static final int PICK_SIGN_IN = 1;

    private AuthorizationContract.Presenter presenter;

    public static AuthorizationFragment newInstance() {
        return new AuthorizationFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SignInButton signInButton = getView().findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_SIGN_IN) {
            presenter.handleSignInResult(resultCode, data);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_authorization;
    }

    @Override
    public void setPresenter(AuthorizationContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public FragmentActivity getAuthorizationActivity() {
        return getActivity();
    }

    @Override
    public void startSignInActivity(Intent intent) {
        startActivityForResult(intent, PICK_SIGN_IN);
    }

    @Override
    public void showSnack(String message) {
        super.showSnack(message);
    }

    @Override
    public void showMessage(String message) {
        TextView messageTextView = getView().findViewById(R.id.message);
        messageTextView.setText(message);
    }

    @OnClick(R.id.sign_in_button)
    void onSignInClick() {
        presenter.signIn();
    }

    @OnClick(R.id.sign_out_button)
    void onSignOut() {
        presenter.signOut();
    }
}
