package com.chancesnow.party;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class QueueFragment extends Fragment {

    private OnQueueFragmentListener mListener;

    public QueueFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnQueueFragmentListener) {
            mListener = (OnQueueFragmentListener) context;
            mListener.onQueueAttached(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnQueueFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnQueueFragmentListener {
        void onQueueAttached(QueueFragment fragment);
    }
}
