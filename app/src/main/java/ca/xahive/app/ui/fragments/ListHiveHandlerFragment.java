package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Observable;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.ui.views.ErrorView;
import ca.xahive.app.ui.views.LoadingView;
import ca.xahive.app.ui.views.NavigationBar;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuListView;

public class ListHiveHandlerFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.xa_hive_list_handler_fragment, container, false);
    }

    public void onStart() {
        super.onStart();
        getEmptyListView().getErrorViewTextView().setText(getEmptyListMessage());
        getLoadingView().setVisibility(View.VISIBLE);
    }

    protected NavigationBar getNavigationBar() {
        return (NavigationBar) getView().findViewById(R.id.navBar);
    }

    protected ErrorView getErrorView() {
        return (ErrorView) getActivity().findViewById(R.id.listHandlerFragmentErrorView);
    }

    protected LoadingView getLoadingView() {
        return (LoadingView) getActivity().findViewById(R.id.listHandlerFragmentLoadingView);
    }

    protected ErrorView getEmptyListView() {
        return (ErrorView) getActivity().findViewById(R.id.listHandlerFragmentEmptyListView);
    }

    protected SwipeMenuListView getListView() {
        return (SwipeMenuListView) getView().findViewById(R.id.listHandlerFragmentListView);
    }

    protected View[] getAllViews() {
        return new View[]{getErrorView(), getLoadingView(), getEmptyListView(), getListView()};
    }

    protected String getEmptyListMessage() {
        // Subclass me.
        return getString(R.string.no_data);
    }

    protected int getListCount() {
        // Subclass me.
        return 0;
    }

    protected void reloadList() {
        // Subclass me.
        return;
    }

    protected void setDataForList(ModelItem modelItem) {
        // Subclass me.
        return;
    }

    @Override
    public void update(Observable observable, Object object) {
        super.update(observable, object);
        getLoadingView().setVisibility(View.GONE);
        if (observable instanceof ModelItem) {
            handleState((ModelItem) observable);
        }
    }

    protected void handleState(ModelItem modelItem) {
        View viewToShow = null;

        switch (modelItem.getState()) {
            case STALE: {
                reloadList();
                break;
            }
            case PENDING: {
                viewToShow = getLoadingView();
                break;
            }
            case CURRENT: {
                setDataForList(modelItem);
                if (getListCount() > 0) {
                    viewToShow = getListView();
                } else {
                    viewToShow = getEmptyListView();
                }
                break;
            }
            case ERROR: {
                viewToShow = getErrorView();
                getErrorView().getErrorViewTextView().setText(modelItem.getError().getReadableCode());
                break;
            }
            default: {
                break;
            }
        }

        if (viewToShow != null) {
            for (View v : getAllViews()) {
                v.setVisibility((v == viewToShow) ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }
}
