package xniuniux.onefignerslyte;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;

import xniuniux.onefignerslyte.MainActivity.AppShortInfo;

public class AppShortcutFragment extends Fragment implements MainActivity.ListenerHolder{

    String LOG_TAG = "AppShortcutFragment";
    private OnFragmentInteractionListener mInteractionListener;
    private CircleListLayout cLayout;
    private ArrayList<Integer> mSelectedApp = new ArrayList<>();

    private View.OnClickListener shortcutFabOnClickListener;

    public AppShortcutFragment() {
    }


    public ArrayList<Integer> getSelectedApp(){
        return mSelectedApp;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            Activity a =(Activity) context;
            mInteractionListener = (OnFragmentInteractionListener) a;

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreateView");
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_app_shortcut, container, false);
        cLayout = (CircleListLayout) rootView.findViewById(R.id.circle_list_layout);

        shortcutFabOnClickListener = destroyFabOnClickListener;

        renewList(null);
        showList();

        return rootView;

    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(LOG_TAG,"onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(LOG_TAG,"onResume");
        if (mSelectedApp != null && mSelectedApp.size()>0 ) {
            renewList(mSelectedApp);
            cLayout.invalidate();
            cLayout.setSelectingMode(false);
        }
    }

    public void renewList(ArrayList<Integer> renewList){
        Log.d(LOG_TAG,"renew list");
        if (renewList == null || renewList.size() == 0){
            renewList = new ArrayList<>();
            for (int i = 0; i < cLayout.getAppShortcutsTotal(); renewList.add(i), i++);
        }
        for (int i : renewList) {
            ImageView button;
            if ( cLayout.getChildCount() > i ){
                button = (ImageView) cLayout.getChildAt(i);
            } else {
                button = (ImageView) LayoutInflater.from(getContext()).
                        inflate(R.layout.element_app_shortcut, null);
                cLayout.addView(button);
                button.setOnClickListener(onClickListener);
                button.setOnLongClickListener(onLongClickListener);
                button.setOnTouchListener(onTouchListener);
            }
            AppShortInfo app = MainActivity.appList.get(i);
            button.setTag(i);
            button.setImageBitmap(app.icons.get(1));
            button.setBackground(null);
        }
        mSelectedApp.clear();
    }

    public void showList(){
        ViewTreeObserver vto = cLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(LOG_TAG,"Global layout OK!");
                cLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Handler handler = new Handler();
                final float startX = cLayout.getWidth()/2;
                final float startY = cLayout.getHeight()/2;
                int layerSelected = cLayout.mLayerSelected;
                int appsPerLayer = cLayout.mAppsPerLayer;
                int childCount = cLayout.getChildCount();
                for ( int i = 0; i < childCount; i++ ){
                    final ImageView child = (ImageView) cLayout.getChildAt((i+ appsPerLayer * layerSelected) % childCount);
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    float deltaX = startX - child.getLeft() - child.getWidth()/2;
                                    float deltaY = startY - child.getTop() - child.getHeight()/2;
                                    child.setVisibility(View.VISIBLE);
                                    ObjectAnimator anima = ObjectAnimator.ofPropertyValuesHolder(
                                            child,
                                            PropertyValuesHolder.ofFloat("translationX", deltaX, 0.0f),
                                            PropertyValuesHolder.ofFloat("translationY", deltaY, 0.0f)
                                    );
                                    anima.setInterpolator(new DecelerateInterpolator());
                                    anima.setDuration(200);
                                    anima.start();
                                }
                            }, i*80);
                }
            }
        });
    }

    public void hideList() {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animatorList = new ArrayList<>();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mInteractionListener.onFragmentInteraction(LOG_TAG, MainActivity.FRG_ACTION_KILL);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        float startX = cLayout.getWidth() / 2;
        float startY = cLayout.getHeight() / 2;
        for (int i = 0; i < cLayout.getChildCount(); i++) {
            View child = cLayout.getChildAt(i);
            float deltaX = startX - child.getLeft() - child.getWidth()/2;
            float deltaY = startY - child.getTop() - child.getHeight()/2;
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                    child,
                    PropertyValuesHolder.ofFloat("translationX", 0.0f, deltaX),
                    PropertyValuesHolder.ofFloat("translationY", 0.0f, deltaY)
            );
            animatorList.add(animator);
        }

        animatorSet.playTogether(animatorList);
        animatorSet.setInterpolator(new AnticipateInterpolator());
        animatorSet.setDuration(200);
        animatorSet.start();
    }


    public View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(cLayout.isSelectingMode()){
                int tag = (int) view.getTag();

                if ( mSelectedApp.contains(tag) ){
                    mSelectedApp.remove((Integer) tag);
                    view.setBackground(null);

                    if (mSelectedApp.size() == 0){
                        cLayout.setSelectingMode(false);
                        shortcutFabOnClickListener = destroyFabOnClickListener;
                        mInteractionListener.onFragmentInteraction(LOG_TAG, MainActivity.FRG_ACTION_LISTENER_UPDATE);
                    }

                } else {
                    if (mSelectedApp.size() > 5){
                        Snackbar.make(cLayout,"你選太多了",Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    mSelectedApp.add(tag);
                    view.setBackground(new BitmapDrawable(getResources(), MainActivity.appList.get((int) view.getTag()).icons.get(3)));
                }
            } else {
                AppShortInfo app = MainActivity.appList.get((int) view.getTag());
                Intent i = getActivity().getPackageManager().getLaunchIntentForPackage(app.name.toString());
                AppShortcutFragment.this.startActivity(i);
            }

        }
    };

    public View.OnLongClickListener onLongClickListener = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View view){
            if(cLayout.isSelectingMode()){ return true; }
            cLayout.setSelectingMode(true);

            shortcutFabOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cLayout.setSelectingMode(false);
                    mInteractionListener.onFragmentInteraction(LOG_TAG, MainActivity.FRG_ACTION_CHANGE_SHORTCUT);
                }
            };
            mInteractionListener.onFragmentInteraction(LOG_TAG, MainActivity.FRG_ACTION_LISTENER_UPDATE);
            onClickListener.onClick(view);
            return  true;
        }
    };

    public View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        float moveFromX;
        float moveFromY;
        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            boolean consume = false;
            ImageView button = (ImageView) view;
            int action = ev.getAction();

            if (action == MotionEvent.ACTION_DOWN && !cLayout.isSelectingMode()) {
                view.setBackground(new BitmapDrawable(getResources(), MainActivity.appList.get((int) view.getTag()).icons.get(2)));
                moveFromX = ev.getX();
                moveFromY = ev.getY();
            }

            if (action == MotionEvent.ACTION_MOVE){
                float moveToX = ev.getX();
                float moveToY = ev.getY();
                //cLayout.moveButton(view, moveToX-moveFromX, moveToY-moveFromY);
            }

            if ((action == MotionEvent.ACTION_UP && !cLayout.isSelectingMode()) || action == MotionEvent.ACTION_CANCEL) {
                if (!cLayout.isSelectingMode()) {
                    button.setBackground(null);
                }
            }
            return consume;
        }
    };

    public View.OnClickListener destroyFabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideList();
        }
    };


    @Override
    public View.OnClickListener getClickListener() {
        return shortcutFabOnClickListener;
    }
}

