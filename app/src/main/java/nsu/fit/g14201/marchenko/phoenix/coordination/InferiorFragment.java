package nsu.fit.g14201.marchenko.phoenix.coordination;


import nsu.fit.g14201.marchenko.phoenix.ui.BaseFragment;

public abstract class InferiorFragment extends BaseFragment {
    private SuperiorActivity superiorActivity;

    public void setSuperiorActivity(SuperiorActivity activity) {
        superiorActivity = activity;
    }

    protected void applyForCoordination() {
        superiorActivity.goToNextView();
    }
}
