package ca.xahive.app.ui.fragments;

import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import java.util.Observable;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.utils.ModelUtils;
import ca.xahive.app.ui.views.ErrorView;
import ca.xahive.app.ui.views.LoadingView;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenu;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuItem;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuListView;

public class ListHandlerFragment extends BaseFragment implements TextWatcher, View.OnClickListener{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.xa_hive_list_handler_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListViewOnClickListener();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getButtonDel().setOnClickListener(this);
        getEditTextSearch().addTextChangedListener(this);
    }

    public void onStart() {
        super.onStart();
        getEmptyListView().getErrorViewTextView().setText(getEmptyListMessage());
        getLoadingView().setVisibility(View.VISIBLE);
    }

    public ImageButton getButtonSearch(){
        return(ImageButton)getView().findViewById(R.id.control_search_btnSearch);
    }

    public ImageButton getButtonDel(){
        return(ImageButton)getView().findViewById(R.id.del_search_btnSearch);
    }

    public AutoCompleteTextView getEditTextSearch() {
        return (AutoCompleteTextView) getView().findViewById(R.id.control_search_Input);
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

    protected void setupListViewOnClickListener() {
        // Subclass me.
        getListView().setClickable(true);
    }

    protected void createSwipeMenuItem(SwipeMenu menu, int drawable, String title) {
        SwipeMenuItem item = new SwipeMenuItem(getActivity());

        item.setBackground(R.color.xa_dark_green);
        item.setWidth(ModelUtils.dp2px(64, getActivity()));
        item.setIcon(drawable);
        item.setTitle(title);
        // remove.setTitleColor(getResources().getColor(R.color.white));
        menu.addMenuItem(item);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.del_search_btnSearch):
                getEditTextSearch().setText("");
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            getButtonDel().setVisibility(View.VISIBLE);
        } else {
            getButtonDel().setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
