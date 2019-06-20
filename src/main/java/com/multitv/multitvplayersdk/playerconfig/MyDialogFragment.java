package com.multitv.multitvplayersdk.playerconfig;

import java.util.Arrays;

import com.multitv.multitvplayersdk.R;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MyDialogFragment extends DialogFragment implements
		OnItemClickListener {

	private String[] listitems;

	private ListView mylist;
	private static ResolutionSelection selection;

	// MyDialogFragment myDialogFragmnt;

	public static MyDialogFragment getInstance(String[] content,
			ResolutionSelection rs) {

		MyDialogFragment dialouge = new MyDialogFragment();
		// String[] stringArray = Arrays.copyOf(content, content.length,
		// String[].class);
		Bundle bundle = new Bundle();
		bundle.putStringArray("Data", content);
		// bundle.putString("Current Selected", selected);
		dialouge.setArguments(bundle);
		selection = rs;

		return dialouge;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.resolution_dialouge_fragment,
				null, false);
		mylist = (ListView) view.findViewById(R.id.list);

		listitems = getArguments().getStringArray("Data");
		// String currentSelected =
		// getArguments().getString("Current Selected");
		setCancelable(true);
		getDialog().setCancelable(true);
		getDialog().setTitle("Select Resolution");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_single_choice, listitems);

		mylist.setAdapter(adapter);
		mylist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		int index = Arrays.asList(listitems).indexOf("currentSelected");
		mylist.setItemChecked(index, true);

		mylist.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		dismiss();
		if (selection != null) {
			selection.onResolutionSelection(listitems[position]);
			mylist.setItemChecked(position, true);
		}
	}

	public interface ResolutionSelection {
		void onResolutionSelection(String index);
	}

}
