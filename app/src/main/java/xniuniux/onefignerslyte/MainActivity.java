package xniuniux.onefignerslyte;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener,
        OnConnectionFailedListener, ConnectionCallbacks, LocationListener {

    /**
     * For debugging
     */
    private String LOG_TAG = "Main Activity";
    public boolean DeBug = false;

    /**
     * Used for interaction with child fragments
     */
    public static final int FRG_ACTION_KILL = -1;
    public static final int FRG_ACTION_CHANGE_SHORTCUT = 1;
    public static final int FRG_ACTION_CONFIRM = 2;
    public static final int FRG_ACTION_LISTENER_UPDATE = 3;

    /**
     * Used to determine the pager style
     */
    public static final int PAGER_STYLE_EMPTY = 0;
    public static final int PAGER_STYLE_WEATHER = 1;

    public int pageNum = 2;
    private ArrayList<Integer> PagerStyle = new ArrayList<>();
    private FragmentManager fm;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    /**
     * For shortcut
     */
    private PackageManager mPm;
    private AppShortcutFragment mAppShortcutFragment = new AppShortcutFragment();
    private AppChooserFragment mAppChooserFragment;
    private ArrayList<ResolveInfo> mLaunchableApps;
    private ArrayList<Integer> mVacancies = new ArrayList<>();
    private ArrayList<Integer> mCandidates = new ArrayList<>();
    public static List<AppShortInfo> appList;

    public class AppShortInfo {
        CharSequence name;
        List<Bitmap> icons;
    }

    /**
     * For floating action button
     */
    private FloatingActionButton fab;
    private boolean isMarginBottomIncreasing = true;
    private boolean isMarginRightIncreasing = true;
    private GestureDetector fabGestureDetector;
    private GestureDetector MainGestureDetector;

    /**
     * Some dimensions
     */
    public int StatusBarHeight;
    private boolean dragMode = false;
    public int MainWidth;
    public int MainHeight;

    /**
     * For Retrieving Google servers API
     */
    private GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final int REQUEST_CODE_UPDATELOCATION = 2;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private LocationRequest mLocationRequest = new LocationRequest();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** solving Google servers API connection error */
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        /** Load pager condition */
        PagerStyle.add(PAGER_STYLE_WEATHER);
        PagerStyle.add(PAGER_STYLE_EMPTY);
        //TODO: pager style should be able to customized and be stored

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        StatusBarHeight = getStatusBarHeight(this);
        if (DeBug) {
            Log.d(LOG_TAG, "status bar: " + StatusBarHeight);
        }

        /** Wall paper */
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        ViewGroup MainView = (ViewGroup) findViewById(R.id.main_content);
        assert MainView != null;
        MainView.setBackground(wallpaperDrawable);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        /** Floating action button */
        fab = (FloatingActionButton) findViewById(R.id.main_fab);
        assert fab != null;
        fab.setOnTouchListener(fabOnTouchListener);
        fabGestureDetector = new GestureDetector(this, new fabGestureListener());

        /** MainActivity gesture detector */
        MainGestureDetector = new GestureDetector(this, new MainGestureListener());

        /** Google API Client */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest.setInterval(60000*15);
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        /** Pager */
        mViewPager = (ViewPager) findViewById(R.id.container);
        assert mViewPager != null;
        mViewPager.setPadding(StatusBarHeight / 2, 0, StatusBarHeight / 2, 0);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setFabClickListener();
            }
        });

        //setSupportActionBar(toolbar);


        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        mPm = getPackageManager();
        mLaunchableApps = (ArrayList<ResolveInfo>) mPm.queryIntentActivities(i, 0);

        mLaunchableApps = sortRIsByLabel(mLaunchableApps, mPm);

        setAppList(null, null);

    }


    @Override
    public void onResume() {
        super.onResume();
        setFabClickListener();
    }

    @Override
    public void onStart(){
        Log.d(LOG_TAG,"onStart");
        mGoogleApiClient.connect();
        Log.d(LOG_TAG,"onStart, connection: " + mGoogleApiClient.isConnected() + ", connecting: " + mGoogleApiClient.isConnecting());
        super.onStart();
    }

    @Override
    public void onStop(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    public ArrayList<ResolveInfo> sortRIsByLabel(ArrayList<ResolveInfo> RIs, PackageManager pm) {
        final PackageManager fPm = pm;
        Collections.sort(RIs, new Comparator<ResolveInfo>() {
            public int compare(ResolveInfo emp1, ResolveInfo emp2) {
                return emp1.loadLabel(fPm).toString().compareToIgnoreCase(emp2.loadLabel(fPm).toString());
            }
        });

        return RIs;
    }

    /**
     * Swap app shortcut
     */
    public void setAppList(int pos, ResolveInfo RI) {
        AppShortInfo app = appList.get(pos);
        app.name = RI.activityInfo.packageName;
        new getIconSet().execute(app, RI);
    }

    public void setAppList(ArrayList<Integer> vacanciesList, ArrayList<Integer> candidatesList) {

        if (appList == null) {
            appList = new ArrayList<>();
        }

        if (vacanciesList == null && candidatesList == null) {
            for (ResolveInfo RI : mLaunchableApps) {
                if (MainActivity.appList.size() > 23) {
                    break;
                }
                AppShortInfo app = new AppShortInfo();
                appList.add(app);
                setAppList(MainActivity.appList.size() - 1, RI);
            }
        }

        if (candidatesList != null && candidatesList.size() > 0) {
            ResolveInfo RI;
            for (int i = 0; i < candidatesList.size(); i++) {
                RI = mLaunchableApps.get(candidatesList.get(i));
                setAppList(vacanciesList.get(i), RI);
            }
        }
    }

    /**
     * Draw shortcuts icon
     */
    public class getIconSet extends AsyncTask<Object, Void, Void> {

        private AppShortInfo appInfo;
        private ResolveInfo ri;

        @Override
        protected Void doInBackground(final Object... param) {
            appInfo = (AppShortInfo) param[0];
            ri = (ResolveInfo) param[1];
            Drawable icon = ri.loadIcon(mPm);
            appInfo.icons = highlightImage(((BitmapDrawable) icon).getBitmap());
            return null;
        }
    }

    public List<Bitmap> highlightImage(Bitmap icon) {

        List<Bitmap> icons = new ArrayList<>();
        Paint ptBlur = new Paint();
        ptBlur.setMaskFilter(new BlurMaskFilter(icon.getWidth() * 0.15f, BlurMaskFilter.Blur.OUTER));
        int[] offsetXY = new int[2];
        Bitmap backLight = icon.extractAlpha(ptBlur, offsetXY);


        Paint ptAlphaColor = new Paint();

        Bitmap iconOut = Bitmap.createBitmap(backLight.getWidth(), backLight.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap highlightWhite = Bitmap.createBitmap(backLight.getWidth(), backLight.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap highlightYellow = Bitmap.createBitmap(backLight.getWidth(), backLight.getHeight(), Bitmap.Config.ARGB_8888);

        ptAlphaColor.setColor(ContextCompat.getColor(this, R.color.long_pressed_highlight));
        Canvas canvas = new Canvas(highlightYellow);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(backLight, 0, 0, ptAlphaColor);

        ptAlphaColor.setColor(ContextCompat.getColor(this, R.color.pressed_highlight));
        canvas = new Canvas(highlightWhite);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(backLight, 0, 0, ptAlphaColor);

        canvas = new Canvas(iconOut);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(icon, -offsetXY[0], -offsetXY[1], null);

        backLight.recycle();

        icons.add(icon);
        icons.add(iconOut);
        icons.add(highlightWhite);
        icons.add(highlightYellow);

        return icons;
    }


    @Override
    public void onFragmentInteraction(String tag, int action) {
        String Log_Tag = "OnFragmentInteraction";
        FragmentManager fm = getSupportFragmentManager();
        if (DeBug) {
            Log.d(Log_Tag, tag);
        }
        if (action == FRG_ACTION_LISTENER_UPDATE) {
            setFabClickListener();
        }
        switch (tag) {
            case "AppShortcutFragment":
                if (action == FRG_ACTION_KILL) {
                    fm.popBackStack();
                    //Log.d(Log_Tag, fm.getBackStackEntryCount() + "");
                    break;
                }
                if (action == FRG_ACTION_CHANGE_SHORTCUT) {
                    mAppChooserFragment = new AppChooserFragment();
                    mAppChooserFragment.setLaunchableAppsRI(mLaunchableApps);
                    mAppChooserFragment.setVacancies(mAppShortcutFragment.getSelectedApp());
                    fm.beginTransaction().replace(R.id.main_content, mAppChooserFragment, "AppChooser")
                            .addToBackStack("AppChooser").commit();
                    break;
                }
            case "AppChooserFragment":
                if (action == FRG_ACTION_CONFIRM) {
                    this.mVacancies = mAppChooserFragment.getVacancies();
                    this.mCandidates = mAppChooserFragment.getCandidates();
                    setAppList(mVacancies, mCandidates);
                    fm.popBackStack();
                    mVacancies = null;
                    mCandidates = null;
                    break;
                }
        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(LOG_TAG, "connection failed");
        if (mResolvingError) {
            // Already attempting to resolve an error.
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //TODO
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public final ArrayList<Fragment> fragments = new ArrayList<>();

        //public int pageNum = 1;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position < getCount()) {
                FragmentManager m = ((Fragment) object).getFragmentManager();
                FragmentTransaction t = m.beginTransaction();
                t.remove((Fragment) object);
                t.commit();
            }
            this.notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            switch (PagerStyle.get(position)) {
                case PAGER_STYLE_WEATHER:
                    return PagerFragmentWeatherForecast.newInstance(position + 1);
                default:
                    return PagerFragmentEmpty.newInstance(position + 1);

            }

        }

        @Override
        public int getCount() {
            return pageNum;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (PagerStyle.get(position)) {
                case PAGER_STYLE_WEATHER:
                    return "Weather";
                case PAGER_STYLE_EMPTY:
                    return "Widget";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }


    /**
     * Get floating action button back whenever double click
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        this.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        MainGestureDetector.onTouchEvent(ev);
        return false;
    }

    private class MainGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            fabBackAnimator();
            return false;
        }
    }

    public void fabBackAnimator() {
        float density = getResources().getDisplayMetrics().density;

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        int mr = lp.rightMargin;
        int mb = lp.bottomMargin;
        float newMr = 16 * density + 0.5f;
        float newMb = 64 * density + 0.5f;
        AnimatorSet animaSet = new AnimatorSet();
        ArrayList<Animator> animaList = new ArrayList<>();
        Animator anima;
        anima = ObjectAnimator.ofInt(this, "FabMarginRight", mr, (int) newMr);
        animaList.add(anima);
        anima = ObjectAnimator.ofInt(this, "FabMarginBottom", mb, (int) newMb);
        animaList.add(anima);
        animaSet.playTogether(animaList);
        animaSet.setDuration(200).setInterpolator(new DecelerateInterpolator());
        animaSet.start();
    }

    /**
     * floating action button touch event
     */
    public View.OnTouchListener fabOnTouchListener = new View.OnTouchListener() {
        float moveFromX;
        float moveFromY;
        float cumulativeMovingThread = 0;
        ViewGroup.MarginLayoutParams lp;
        int mr;
        int mb;

        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            fabGestureDetector.onTouchEvent(ev);
            boolean consume = false;
            int action = ev.getAction();
            lp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            mr = lp.rightMargin;
            mb = lp.bottomMargin;

            if (action == MotionEvent.ACTION_DOWN) {
                cumulativeMovingThread = 0;
                moveFromX = ev.getX();
                moveFromY = ev.getY();
            }

            if (action == MotionEvent.ACTION_MOVE) {

                float moveToX = ev.getX();
                float moveToY = ev.getY();
                if (!dragMode) {
                    if ((cumulativeMovingThread > fab.getWidth() / 2)) {
                        dragMode = true;
                    }
                    cumulativeMovingThread += Math.abs(moveToX - moveFromX)
                            + Math.abs(moveToY - moveFromY);
                }
                isMarginRightIncreasing = moveToX < moveFromX;
                isMarginBottomIncreasing = moveToY < moveFromY;
                setFabMarginRight((int) (mr + moveFromX - moveToX));
                setFabMarginBottom((int) (mb + moveFromY - moveToY));
                consume = false;
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (dragMode) {
                    consume = true;
                    dragMode = false;
                }
            }
            return consume;
        }
    };

    private class fabGestureListener extends GestureDetector.SimpleOnGestureListener {
        String Log_Tag = "gesture";

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
            float absDMR = Math.abs(vX / 3);
            float absDMB = Math.abs(vY / 3);
            float dmr = isMarginRightIncreasing ? absDMR : -absDMR;
            float dmb = isMarginBottomIncreasing ? absDMB : -absDMB;
            fabFlingAnimator((int) dmr, (int) dmb, 120);
            return true;
        }
    }

    public void setFabClickListener() {
        Fragment frg = getCurrentFragment();
        if (frg == null) {
            fab.setOnClickListener(this.mainFabOnClickListener);
            if (!fab.isShown()) {
                fab.show();
            }
            return;
        }
        if (frg instanceof ListenerHolder) {
            View.OnClickListener listener = ((ListenerHolder) frg).getClickListener();
            if (listener != null) {
                fab.setOnClickListener(listener);
                if (!fab.isShown()) {
                    fab.show();
                }
                return;
            }
        }
        fab.hide();
    }

    public Fragment getCurrentFragment() {

        if (fm.getBackStackEntryCount() == 0) {
            return null;
        }
        return fm.findFragmentByTag(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName());

    }

    public void setFabMarginRight(int d) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        lp.rightMargin = d;
        fab.setLayoutParams(lp);
    }

    public void setFabMarginBottom(int d) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        lp.bottomMargin = d;
        fab.setLayoutParams(lp);
    }

    public void fabFlingAnimator(int dmr, int dmb, int duration) {
        MainWidth = findViewById(R.id.main_content).getWidth();
        MainHeight = findViewById(R.id.main_content).getHeight();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        int halfW = fab.getWidth() / 2;
        int halfH = fab.getHeight() / 2;
        int mr = lp.rightMargin;
        int mb = lp.bottomMargin;
        int newMr = mr + dmr;
        int newMb = mb + dmb;
        newMr = Math.max(newMr, -halfW);
        newMr = Math.min(newMr, MainWidth - halfW);
        newMb = Math.max(newMb, -halfH);
        newMb = Math.min(newMb, MainHeight - halfH);
        AnimatorSet animaSet = new AnimatorSet();
        ArrayList<Animator> animaList = new ArrayList<>();
        Animator anima;
        anima = ObjectAnimator.ofInt(this, "FabMarginRight", mr, newMr);
        animaList.add(anima);
        anima = ObjectAnimator.ofInt(this, "FabMarginBottom", mb, newMb);
        animaList.add(anima);
        animaSet.playTogether(animaList);
        animaSet.setDuration(duration).setInterpolator(new DecelerateInterpolator());
        animaSet.start();
    }

    private View.OnClickListener mainFabOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            FragmentManager fm = getSupportFragmentManager();
            if (!mAppShortcutFragment.isAdded()) {
                fm.beginTransaction()
                        .add(R.id.main_content, mAppShortcutFragment, "appShortcut")
                        .addToBackStack("appShortcut")
                        .commit();
            }

        }
    };

    public interface ListenerHolder {
        View.OnClickListener getClickListener();
    }


    private int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        java.lang.reflect.Field field;
        int x;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    public int getStatusBarHeight() {
        return StatusBarHeight;
    }


    public void updateLocation() {
        Log.d(LOG_TAG, "updatelocation, " + mGoogleApiClient.isConnected());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request missing location permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_UPDATELOCATION);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                mLastLocation = location;
            } else{
                Log.d(LOG_TAG, "location is null, request update");
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location has changed");

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_UPDATELOCATION) {
            if (permissions.length == 2 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG,"permission grant");
                this.updateLocation();
            } else {
                Log.d(LOG_TAG, "permission denied");
            }
        }

    }

    public HashMap<String, Float> getLocation() {
        HashMap<String, Float> absLocation = new HashMap<>();
        if(mLastLocation != null) {
            Log.d(LOG_TAG, "absLocation not null");
            absLocation.put("longitude", (float) mLastLocation.getLongitude());
            absLocation.put("latitude", (float) mLastLocation.getLatitude());
        }

        return absLocation;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "onConnect client connected: " + mGoogleApiClient.isConnected());
        this.updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}


