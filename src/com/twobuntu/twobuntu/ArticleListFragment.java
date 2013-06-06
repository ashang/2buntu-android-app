package com.twobuntu.twobuntu;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

import com.twobuntu.article.ArticleAdapter;
import com.twobuntu.article.ArticleAdapter.RefreshListener;

// Displays the list of articles on the home page.
public class ArticleListFragment extends ListFragment {

	// Used for storing the ID of the current article.
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	
	// Interface for providing notification of list item selections.
	public interface ArticleSelectedListener {
		void onArticleSelected(int id);
	}
	
	// The current listener for article selection events.
	private ArticleSelectedListener mListener;

	// The currently selected article.
	private int mActivatedPosition = ListView.INVALID_POSITION;

	public ArticleListFragment() {
	}

	// The adapter used for retrieving article information.
	private ArticleAdapter mAdapter;
	
	// Begins loading the list of articles.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// This fragment has a menu and should be retained during configuration changes.
		setHasOptionsMenu(true);
		setRetainInstance(true);
		// Create the adapter and set it when the refresh completes.
		mAdapter = new ArticleAdapter(getActivity());
		mAdapter.setOnRefreshListener(new RefreshListener() {
			
			@Override
			public void onRefresh() {
				setListAdapter(mAdapter);
			}
		});
		mAdapter.refresh(getActivity());
	}

	// Restores the previously selected article if possible.
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if(savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
	}

	// Grab a pointer to the listener for article selection events.
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(!(activity instanceof ArticleSelectedListener))
			throw new IllegalStateException("Activity must implement ArticleSelectedListener.");
		mListener = (ArticleSelectedListener)activity;
	}
	
	// Add the "toolbar" buttons to the action bar.
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.activity_article_list, menu);
	}

	// When the activity is destroyed, remove the callback.
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	// Process click events for the list view..
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		if(mListener != null)
		    mListener.onArticleSelected(mAdapter.getItem(position).mId);
	}

	// Save the currently selected article.
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mActivatedPosition != ListView.INVALID_POSITION)
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
	}

	// This causes list items to enter the activated state when clicked.
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		getListView().setChoiceMode(activateOnItemClick?
				ListView.CHOICE_MODE_SINGLE:
				ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if(position == ListView.INVALID_POSITION)
			getListView().setItemChecked(mActivatedPosition, false);
		else
			getListView().setItemChecked(position, true);
		mActivatedPosition = position;
	}
}
