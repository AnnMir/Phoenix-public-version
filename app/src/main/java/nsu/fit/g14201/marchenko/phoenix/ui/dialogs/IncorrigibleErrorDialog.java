package nsu.fit.g14201.marchenko.phoenix.ui.dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class IncorrigibleErrorDialog extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    public static IncorrigibleErrorDialog newInstance(@Nullable String title, @NonNull String message) {
        IncorrigibleErrorDialog dialog = new IncorrigibleErrorDialog();

        Bundle args = new Bundle();
        if (title != null) {
            args.putString(ARG_TITLE, message);
        }
        args.putString(ARG_MESSAGE, message);
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String title = getArguments().getString(ARG_TITLE);
        if (title != null) {
            builder.setTitle(title);
        }

        builder.setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok, null);

        return builder.create();
    }
}
