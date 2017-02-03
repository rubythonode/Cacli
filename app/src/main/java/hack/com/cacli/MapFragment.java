package hack.com.cacli;


import android.location.Address;
import android.location.Location;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.location.Geocoder;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements NMapPOIdataOverlay.OnStateChangeListener{

    private NMapContext mMapContext;
    private static final String CLIENT_ID = "HTSOdNC5nUu2HRqBtirR";
    private Geocoder mGeocoder;
    private MapActivity mapActivity;
    private MapOverlayController mapOverlayController;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment getInstance(MapActivity mapActivity){
        MapFragment mapFragment = new MapFragment();
        mapFragment.mapActivity = mapActivity;

        return mapFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapContext = new NMapContext(super.getActivity());
        mGeocoder = new Geocoder(getActivity(), Locale.KOREA);
        mMapContext.onCreate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final NMapView mapView = (NMapView)getView().findViewById(R.id.mapView);
        initMapView(mapView);

        final NMapViewerResourceProvider mMapViewerResourceProvider = new NMapViewerResourceProvider(getActivity());
        final NMapOverlayManager mapOverlayManager = new NMapOverlayManager(getActivity(), mapView, mMapViewerResourceProvider);

        //mapView.getMapController().setMapCenter(findGeoPoint("강남역"),13);

        GPSModule gpsModule = new GPSModule(getActivity(), new GPSModule.OnSuccessListener() {
            @Override
            public void success(Location location) {
                if(location == null) {
                    Toast.makeText(getActivity(), "GPS을 확인해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i("info", String.format(Locale.KOREA, "위도 : %s 경도 : %s", String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude())));
                NGeoPoint point = new NGeoPoint(location.getLongitude(), location.getLatitude());
                mapView.getMapController().setMapCenter(point, 13);

                mapOverlayController = new MapOverlayController(mMapViewerResourceProvider, mapOverlayManager);
                List<OverlayItem> overlayItems = new ArrayList<>();
                overlayItems.add(new OverlayItem(location.getLongitude(), location.getLatitude(), NMapPOIflagType.TO, "유저"));
                overlayItems.add(new OverlayItem(location.getLongitude(), location.getLatitude()+0.001, NMapPOIflagType.FROM, "건물1"));
                overlayItems.add(new OverlayItem(location.getLongitude(), location.getLatitude()+0.002, NMapPOIflagType.FROM, "건물1"));
                mapOverlayController.initOverlayItemList(overlayItems);
                mapOverlayController.displayOverlayItemList(MapFragment.this);
            }
        });


        gpsModule.getCurrentLocation();

    }

    private void initMapView(NMapView mapView) {
        mapView.setClientId(CLIENT_ID);
        mapView.setClickable(true);
        mapView.setEnabled(true);
        mapView.setFocusable(true);
        mapView.setFocusableInTouchMode(true);
        mapView.requestFocus();
        mMapContext.setupMapView(mapView);
    }

    /**
     * 주소로부터 위치정보 취득
     * @param address 주소
     */
    private NGeoPoint findGeoPoint(String address) {
        Geocoder geocoder = new Geocoder(getActivity());
        Address addr;
        NGeoPoint location = null;

        Log.i("info","findGeoPoint");

        try {
            List<Address> listAddress = geocoder.getFromLocationName(address, 1);
            if (listAddress.size() > 0) { // 주소값이 존재 하면
                addr = listAddress.get(0); // Address형태로
                int lat = (int) ((addr.getLatitude()) * 1E6);
                int lng = (int) ((addr.getLongitude()) * 1E6);
                location = new NGeoPoint(lng, lat);

                Log.i("info", "주소로부터 취득한 위도 : " + lat + ", 경도 : " + lng);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapOverlayController.clearCalloutOverlay();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapContext.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapContext.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapContext.onPause();
    }

    @Override
    public void onStop() {
        mMapContext.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mMapContext.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
        if(nMapPOIitem == null)return;

        NGeoPoint point = nMapPOIitem.getPoint();

        if(point == null)return;

        if("유저".equals(nMapPOIitem.getTag()))return;

        try {
            List<Address> addressList = mGeocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
            if(addressList != null && addressList.size() > 0){
                mapActivity.callBottomSheet(nMapPOIitem.getTag().toString(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

    }

    public class OverlayItem{
        private final double longitude;
        private final double latitude;
        private final int type;
        private final String tag;

        public OverlayItem(double longitude, double latitude, int type, String tag) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.type = type;
            this.tag = tag;
        }
    }

    public class MapOverlayController{
        private final NMapViewerResourceProvider mMapViewerResourceProvider;
        private final NMapOverlayManager mMapOverlayManager;
        private final NMapPOIdata mPOIData;

        public MapOverlayController(NMapViewerResourceProvider mapViewerResourceProvider, NMapOverlayManager mMapOverlayManager){
            this.mMapViewerResourceProvider = mapViewerResourceProvider;
            this.mMapOverlayManager = mMapOverlayManager;
            this.mPOIData = new NMapPOIdata(2, mMapViewerResourceProvider);
        }

        public void initOverlayItemList(List<OverlayItem> itemList){
            mPOIData.beginPOIdata(itemList.size());
            for(OverlayItem item : itemList){
                mPOIData.addPOIitem(item.longitude, item.latitude, null, item.type, item.tag);
            }
            mPOIData.endPOIdata();
        }

        public void displayOverlayItemList(NMapPOIdataOverlay.OnStateChangeListener onStateChangeListener){
            NMapPOIdataOverlay poIdataOverlay = mMapOverlayManager.createPOIdataOverlay(mPOIData, null);
            poIdataOverlay.setOnStateChangeListener(onStateChangeListener);
        }

        public void clearCalloutOverlay(){
            mMapOverlayManager.clearCalloutOverlay();
        }
    }
}
