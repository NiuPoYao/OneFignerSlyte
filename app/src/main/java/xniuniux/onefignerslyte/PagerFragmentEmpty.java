package xniuniux.onefignerslyte;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PagerFragmentEmpty#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PagerFragmentEmpty extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int mSectionNumber;

    public PagerFragmentEmpty() {
        // Required empty public constructor
    }

    public static PagerFragmentEmpty newInstance(int sectionNumber) {
        PagerFragmentEmpty fragment = new PagerFragmentEmpty();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_section, container, false);


        return rootView;
    }




}
