package xniuniux.onefignerslyte;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.plus.PlusOneButton;
import com.google.android.gms.vision.text.Text;

import java.util.List;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link AppChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AppChooserFragment extends Fragment {

    private String LOG_TAG = "AppChooserFragment";

    private FloatingActionButton mConfirmFab;

    private OnFragmentInteractionListener mListener;

    public AppChooserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_chooser, container, false);

        GridView appList = (GridView) view.findViewById(R.id.app_chooser_grid_list);

        PackageManager pm = getActivity().getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> launchableApps = pm.queryIntentActivities(i, 0);

        appList.setAdapter(new appAdapter(getContext(), launchableApps));

        mConfirmFab = (FloatingActionButton) view.findViewById(R.id.fab_confirm);
        mConfirmFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentInteraction(LOG_TAG, MainActivity.FRG_ACTION_CONFIRM);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
    }

    public class appAdapter extends BaseAdapter{
        private Context mContext;
        private List<ResolveInfo> mRIs;

        public appAdapter(Context context, List<ResolveInfo> list){
            mContext = context;
            mRIs = list;
        }

        public int getCount() {
            return mRIs.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }


        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            //int width = parent.getWidth()/5;
            ImageView button;
            TextView textView;

            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.app_list, null);
                //convertView.setLayoutParams(new GridView.LayoutParams(width,width));

            }

            button = (ImageView) convertView.findViewById(R.id.app_list_imagebutton);
            textView = (TextView) convertView.findViewById(R.id.app_list_text);

            button.setImageBitmap( ((BitmapDrawable) mRIs.get(position).
                    activityInfo.loadIcon(mContext.getPackageManager())).getBitmap() );
            textView.setText(mRIs.get(position).loadLabel(mContext.getPackageManager()));
            convertView.setTag(position);

            return convertView;
        }


    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String tag, int action);
    }

}
