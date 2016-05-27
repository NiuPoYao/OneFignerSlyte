package xniuniux.onefignerslyte;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import xniuniux.onefignerslyte.MainActivity.AppShortInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppShortcutFragment extends Fragment {

    String LOG_TAG = "AppShortcutFragment";
    public FragmentSuicideListener suicideListener;
    public CircleListLayout cLayout;
    public ArrayList<AppShortInfo> appArrayList;


    public AppShortcutFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            Activity a =(Activity) context;
            suicideListener = (FragmentSuicideListener) a;

        }
    }

    public void setAppList(ArrayList<AppShortInfo> list ){
        this.appArrayList = list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_app_shortcut, container, false);
        cLayout = (CircleListLayout) rootView.findViewById(R.id.circle_list_layout);

        for (int i = 0; i < cLayout.getChildCount(); i++){
            ImageButton button = (ImageButton) cLayout.getChildAt(i);
            AppShortInfo app = appArrayList.get(i);
            app.layoutId = button.getId();
            button.setImageBitmap(app.icons.get(0));
            button.setSelected(true);
            button.setOnClickListener(onClickListener);
            button.setOnLongClickListener(onLongClickListener);
            button.setOnTouchListener(onTouchListener);
        }
        return rootView;

    }

    @Override
    public void onStart(){
        super.onStart();
        showList();
    }

    public void showList(){
        final CircleListLayout CLayout = cLayout;
        ViewTreeObserver vto = CLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                CLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Handler handler = new Handler();
                final float startX = CLayout.getWidth()/2;
                final float startY = CLayout.getHeight()/2;
                for ( int i = 0; i < CLayout.getChildCount(); i++ ){
                    final View child = CLayout.getChildAt(i);
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
                            }, i*100);
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
                suicideListener.onFragmentSuicide("appList");
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
            int id = view.getId();
            Toast.makeText(getContext(), "click " + view.toString(),
                    Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG,"onClick");
            /*for (AppShortInfo app: appArrayList){
                if(app.layoutId == id){
                    Intent i = packageManager.getLaunchIntentForPackage(app.name.toString());
                    AppShortcutFragment.this.startActivity(i);
                }
            }*/

        }
    };

    public View.OnLongClickListener onLongClickListener = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View view){
            Toast.makeText(getContext(), "long click " + view.toString(),
                    Toast.LENGTH_SHORT).show();
            cLayout.setRotateEnable(false);
            int id = view.getId();
            for (MainActivity.AppShortInfo app : appArrayList) {
                if (app.layoutId == id) {
                    view.setBackground(new BitmapDrawable(getResources(), app.icons.get(2)));
                }
            }
            Log.d(LOG_TAG,"onLongClick");
            return  true;
        }
    };

    public View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        float moveFromX;
        float moveFromY;
        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            boolean consume = true;
            ImageButton button = (ImageButton) view;
            int action = ev.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                moveFromX = ev.getX();
                moveFromY = ev.getY();
                int id = view.getId();
                for (MainActivity.AppShortInfo app : appArrayList) {
                    if (app.layoutId == id) {
                        button.setBackground(new BitmapDrawable(getResources(), app.icons.get(1)));
                    }
                }
                consume = false;
            }

            if (action == MotionEvent.ACTION_MOVE){
                float moveToX = ev.getX();
                float moveToY = ev.getY();
                cLayout.moveButton(view, moveToX-moveFromX, moveToY-moveFromY);
                Log.d(LOG_TAG, "Drag from " + moveFromX + " to " +
                        moveToX + ", " + view.getWidth());
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                int id = view.getId();
                cLayout.setRotateEnable(true);
                for (MainActivity.AppShortInfo app : appArrayList) {
                    if (app.layoutId == id) {
                        button.setBackgroundColor(0x00000000);
                    }
                }
                consume = false;
            }
            Log.d(LOG_TAG, "view onTouch return: " + consume + ev.toString());
            return consume;
        }
    };


}

