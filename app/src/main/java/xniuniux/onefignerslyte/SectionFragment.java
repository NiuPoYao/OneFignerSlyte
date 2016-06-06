package xniuniux.onefignerslyte;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class SectionFragment extends Fragment {

    private String LOG_TAG = "pagerFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    public SectionFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SectionFragment newInstance(int sectionNumber) {
        SectionFragment fragment = new SectionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section, container, false);
        View bodyView = rootView.findViewById(R.id.pager_body);
        View bannerView = rootView.findViewById(R.id.pager_banner);
        ViewGroup.LayoutParams params =  bodyView.getLayoutParams();
        Log.d(LOG_TAG, " " + rootView.getWidth() );
        return rootView;
    }
}
