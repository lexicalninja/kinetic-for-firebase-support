package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.TrainingPlan;
import com.kinetic.fit.data.realm_objects.Workout;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

/**
 * Created by Saxton on 12/20/16.
 */

public class FitSearchBar extends LinearLayout {

    LayoutInflater mInflater;
    ImageView cancelButton;
    ImageView searchButton;
    EditText searchText;

    FitSearchListener mListener;

    List<? extends RealmObject> mObjects;

    public interface FitSearchListener {
        void searchObjectsUpdated(List<? extends RealmObject> objects);

        void cancelSearch();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FitSearchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mInflater = LayoutInflater.from(getContext());
        init(context);
    }

    public FitSearchBar(Context context) {
        super(context);
        mInflater = LayoutInflater.from(getContext());
        init(context);
    }

    public FitSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(getContext());
        init(context);
    }

    public FitSearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(getContext());
        init(context);
    }

    void init(Context context) {
        View.inflate(context, R.layout.fit_search_bar, this);

        cancelButton = (ImageView) findViewById(R.id.search_bar_cancel_button);
        searchButton = (ImageView) findViewById(R.id.search_bar_execute_search_button);
        searchText = (EditText) findViewById(R.id.search_bar_search_text);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.searchObjectsUpdated(mObjects);
                    mListener.cancelSearch();
                    hideKeyBoard();
                    searchText.setText("");
                }
            }
        });
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<RealmObject> newObjects = new ArrayList<>();
                if (mObjects != null) {
                    for (RealmObject o : mObjects) {
                        if (o instanceof Workout) {
                            if (((Workout) o).getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                                newObjects.add(o);
                            }
                        } else if (o instanceof TrainingPlan) {
                            if (((TrainingPlan) o).getIsHeader()) {
                                newObjects.add(o);
                            } else if (((TrainingPlan) o).getPlanName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                                newObjects.add(o);
                            }
                        }
                    }
                    if (mListener != null) {
                        mListener.searchObjectsUpdated(newObjects);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    hideKeyBoard();
                    return true;
                } else {
                    return false;
                }
            }
        });

        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();
            }
        });

    }

    public void setListener(FitSearchListener listener) {
        mListener = listener;
    }

    public void setData(List<? extends RealmObject> objects) {
        mObjects = objects;
    }

    public void hideSearchBar() {
        this.setVisibility(GONE);
    }

    public void showSearchBar() {
        this.setVisibility(VISIBLE);
        searchText.requestFocus();
    }

    void hideKeyBoard() {
        InputMethodManager imm = ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    public void clear() {
        searchText.setText("");
    }
}
