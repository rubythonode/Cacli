package hack.com.cacli;


import android.location.Address;
import android.location.Location;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.location.Geocoder;
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
    private GPSModule gpsModule;
    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment getInstance(){
        MapFragment mapFragment = new MapFragment();

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

                MapOverlayController mapOverlayController = new MapOverlayController(mMapViewerResourceProvider, mapOverlayManager);
                List<OverlayItem> overlayItems = new ArrayList<>();
                overlayItems.add(new OverlayItem(location.getLongitude(), location.getLatitude(), "Pizza 777-111", NMapPOIflagType.FROM, "tag1"));
                overlayItems.add(new OverlayItem(location.getLongitude(), location.getLatitude()+0.001, "Pizza 777-222", NMapPOIflagType.TO, "tag2"));
                mapOverlayController.initOverlayItemList(overlayItems);
                mapOverlayController.displayOverlayItemList(MapFragment.this);

                /*int markerId = NMapPOIflagType.PIN;

                // set POI data
                NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
                poiData.beginPOIdata(1);
                poiData.addPOIitem(location.getLongitude(), location.getLatitude(), "Pizza 777-111", NMapPOIflagType.FROM, "tag1");
                poiData.addPOIitem(location.getLongitude(), location.getLatitude()+0.001, "Pizza 777-222", NMapPOIflagType.TO, "tag2");
                poiData.endPOIdata();

                // create POI data overlay
                NMapPOIdataOverlay poiDataOverlay = mapOverlayManager.createPOIdataOverlay(poiData, null);
                // show all POI data
                poiDataOverlay.showAllPOIdata(0);
                //set event listener to the overlay
                poiDataOverlay.setOnStateChangeListener(new NMapPOIdataOverlay.OnStateChangeListener() {
                    @Override
                    public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {

                    }

                    @Override
                    public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
                        NGeoPoint point = nMapPOIitem.getPoint();
                        Log.i("info", "call");
                        try {
                            List<Address> addressList = mGeocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                            if(addressList != null && addressList.size() > 0){
                                Toast.makeText(getActivity(), String.format(Locale.KOREA,"%s 현재 위치의 주소는 %s", nMapPOIitem.getTag(),addressList.get(0).getAddressLine(0).toString()), Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });*/
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

    }

    @Override
    public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
        NGeoPoint point = nMapPOIitem.getPoint();
        try {
            List<Address> addressList = mGeocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
            if(addressList != null && addressList.size() > 0){
                Toast.makeText(getActivity(), String.format(Locale.KOREA,"%s 현재 위치의 주소는 %s", nMapPOIitem.getTag(), addressList.get(0).getAddressLine(0).toString()), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class OverlayItem{
        private final double longitude;
        private final double latitude;
        private final String title;
        private final int type;
        private final String tag;

        public OverlayItem(double longitude, double latitude, String title, int type, String tag) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.title = title;
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
                mPOIData.addPOIitem(item.longitude, item.latitude, item.title, item.type, item.tag);
            }
            mPOIData.endPOIdata();
        }

        public void displayOverlayItemList(NMapPOIdataOverlay.OnStateChangeListener onStateChangeListener){
            NMapPOIdataOverlay poIdataOverlay = mMapOverlayManager.createPOIdataOverlay(mPOIData, null);
            poIdataOverlay.setOnStateChangeListener(onStateChangeListener);
        }
    }
}
