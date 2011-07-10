/*
 * Copyright (C) 2011 Brian Reber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by Brian Reber.  
 * THIS SOFTWARE IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.reber.agenda;

import org.reber.agenda.list.AgendaListFragment;
import org.reber.agenda.list.CalendarListFragment;
import org.reber.agenda.util.Constants;
import org.reber.agenda.util.Util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * This is the Activity that gets loaded when the user clicks on the app icon,
 * just like they would do to open any other application.
 * 
 * @author brianreber
 */
public class AgendaActivity extends FragmentActivity {
	
	private AgendaListFragment frag;
	private CalendarListFragment calfrag;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		setResult(RESULT_OK, new Intent(AgendaWidgetProvider.WIDGET_UPDATE));
		
		final SharedPreferences pref = getSharedPreferences(Constants.AgendaList.APP_PREFS, Activity.MODE_WORLD_WRITEABLE);
		if (getResources().getIntArray(R.array.versions)[0] > pref.getInt(Constants.AgendaList.VERSION, 0)) {
			Dialog dlg = new Dialog(this);
			dlg.setTitle(getResources().getString(R.string.changes));

			TextView tv = new TextView(this);
			tv.setText(R.string.updateMessage);
			tv.setWidth(getWindowManager().getDefaultDisplay().getWidth() - 10);
			tv.setPadding(5, 0, 5, 5);
			dlg.setContentView(tv);
			dlg.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					Editor edit = pref.edit();
					edit.putInt(Constants.AgendaList.VERSION, getResources().getIntArray(R.array.versions)[0]);
					edit.commit();
				}
			});
			dlg.show();
		}
		
		frag = (AgendaListFragment) getSupportFragmentManager().findFragmentById(R.id.list_frag);
		calfrag = (CalendarListFragment) getSupportFragmentManager().findFragmentById(R.id.cal_list_frag);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (frag != null) {
			frag.notifyUtilUpdated();
		}
	}

	/**
	 * Gets called when the menu button is pressed.
	 * 
	 * @param menu The menu instance that we apply a menu to
	 * @return true so that it uses our own implementation
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.appmenu, menu);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		if (!Util.isIntentAvailable(this, intent)) {
			menu.removeItem(R.id.newEventMenuItem);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuOpened(int, android.view.Menu)
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.refresh) {
			frag.notifyUtilUpdated();
		} else if (id == R.id.chooseSettings) {
			Intent temp = new Intent(Intent.ACTION_CHOOSER);
			temp.setClass(this, SettingsActivity.class);
			startActivityForResult(temp, 10000);
		} else if (id == R.id.newEventMenuItem) {
			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");
			
			startActivity(intent);
		}

		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 10000 && resultCode != RESULT_CANCELED) {
			frag.notifyUtilUpdated();
			
			if (calfrag != null) {
				calfrag.setPrefToGrabFrom(Constants.AgendaList.APP_PREFS);
			}
		}
	}
}