/*
 * Copyright (C) 2015 - 2016 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.vrem.wifianalyzer.wifi.graph.channel;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.jjoe64.graphview.GraphView;
import com.vrem.wifianalyzer.Configuration;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.graph.channel.ChannelGraphNavigation.NavigationItem;
import com.vrem.wifianalyzer.wifi.scanner.Broadcast;
import com.vrem.wifianalyzer.wifi.scanner.Receiver;
import com.vrem.wifianalyzer.wifi.scanner.Scanner;

public class ChannelGraphFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private Receiver receiver;
    private Broadcast broadcast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.graph_content, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.graphRefresh);
        swipeRefreshLayout.setOnRefreshListener(new ListViewOnRefreshListener());

        Configuration configuration = MainContext.INSTANCE.getConfiguration();
        ChannelGraphNavigation channelGraphNavigation = new ChannelGraphNavigation(getActivity(), configuration);
        ChannelGraphAdapter channelGraphAdapter = new ChannelGraphAdapter(channelGraphNavigation);
        addGraphViews(swipeRefreshLayout, channelGraphAdapter);
        addGraphNavigation(view, channelGraphNavigation);

        receiver = new Receiver(channelGraphAdapter);

        broadcast = new Broadcast();
        broadcast.register(getActivity(), receiver);

        return view;
    }

    private void addGraphNavigation(View view, ChannelGraphNavigation channelGraphNavigation) {
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.graphNavigation);
        for (NavigationItem navigationItem : channelGraphNavigation.getNavigationItems()) {
            linearLayout.addView(navigationItem.getButton());
        }
    }

    private void addGraphViews(View view, ChannelGraphAdapter channelGraphAdapter) {
        ViewFlipper viewFlipper = (ViewFlipper) view.findViewById(R.id.graphFlipper);
        for (GraphView graphView : channelGraphAdapter.getGraphViews()) {
            viewFlipper.addView(graphView);
        }
    }

    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        Scanner scanner = MainContext.INSTANCE.getScanner();
        scanner.update();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        broadcast.register(getActivity(), receiver);
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcast.unregister(getActivity(), receiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        broadcast.unregister(getActivity(), receiver);
    }

    private class ListViewOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refresh();
        }
    }

}
