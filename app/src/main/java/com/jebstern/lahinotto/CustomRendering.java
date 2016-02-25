package com.jebstern.lahinotto;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class CustomRendering extends DefaultClusterRenderer<MarkerBean> {

    public CustomRendering(Context context, GoogleMap map, ClusterManager<MarkerBean> clusterManager) {
        super(context, map, clusterManager);
    }


    protected void onBeforeClusterItemRendered(MarkerBean item, MarkerOptions markerOptions) {
        markerOptions.snippet(item.getAddress());
        markerOptions.title(item.getLocationPlace());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}