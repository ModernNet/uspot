package com.modernet.uspot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.software.shell.fab.ActionButton;

import java.util.ArrayList;


public class TabFragment extends Fragment {
    private static final String TAG = "TabFragment";
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<InterestPoint> mDataset;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    int position;
    private ActionButton fab;
    private String [] categories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"TabFragment onCreate()");
        super.onCreate(savedInstanceState);
        categories= getResources().getStringArray(R.array.categories);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Era això, collons!!!
        if (mDataset == null) mDataset = getArguments().getParcelableArrayList("Dataset");
        position = getArguments().getInt("position");
        return inflater.inflate(R.layout.tab_layout, container, false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh();
            }
        });

        fab = (ActionButton) getActivity().findViewById(R.id.fab_button);
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // specify an adapter (see also next example)
        if (mDataset != null) {
            mAdapter = new MyAdapter(mDataset,getActivity());
            mRecyclerView.setAdapter(mAdapter);
        }

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0 && fab.isShown()) {
                    Log.i(TAG,"Hiding Button");
                    fab.hide();
                }else if (dy<0 && fab.isHidden()) {
                    Log.i(TAG,"Showing Button");
                    fab.show();
                }
            }
        });
    }

    public void initiateRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Refresh Tab
                    TabFragment fragment = (TabFragment)((MainActivity)getActivity()).mSectionsPagerAdapter.getFragment(position);
                    Get get = ((MainActivity)getActivity()).get;
                    get.getResponse();
                    fragment.mDataset = get.getCategoryDataset(categories[position]);
                    fragment.mAdapter = new MyAdapter(fragment.mDataset,fragment.getActivity());
                    fragment.mRecyclerView.setAdapter(fragment.mAdapter);
                    Log.d(TAG,"Updating TAB #" + position);
                }
                else
                    Toast.makeText(
                            getActivity(),
                            "No network connection available.",
                            Toast.LENGTH_SHORT
                    ).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }
}
