package com.chancesnow.party;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass displaying a indeterminate progress spinner.
 */
public class LoadingFragment extends Fragment {

    private String mTopic;
    private TextView mLoadingLabel;

    public LoadingFragment() {
        mTopic = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getView() != null) {
            mLoadingLabel = (TextView) getView().findViewById(R.id.loading_loading);

            if (mTopic != null) {
                mLoadingLabel.setText(getString(R.string.loading_topic, mTopic));
            }
        }
    }

    public void setLoadingTopic(String topic) {
        mTopic = topic;

        if (getView() != null && mLoadingLabel != null) {
            if (mTopic != null) {
                mLoadingLabel.setText(getString(R.string.loading_topic, mTopic));
            } else {
                mLoadingLabel.setText(getString(R.string.loading));
            }
        }
    }
}
