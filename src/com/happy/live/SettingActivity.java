package com.happy.live;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

import com.umeng.analytics.MobclickAgent;

public class SettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	CheckBoxPreference softDecodeCheckBox = null, p2pCHeckBox = null;
	Preference title = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
		this.getListView().setBackgroundResource(R.drawable.list_bg);
		softDecodeCheckBox = (CheckBoxPreference) findPreference(getString(R.string.soft_decode));
		softDecodeCheckBox.setOnPreferenceChangeListener(this);
		p2pCHeckBox = (CheckBoxPreference) findPreference(getString(R.string.setting_p2p));
		p2pCHeckBox.setOnPreferenceChangeListener(this);

		title = findPreference("setting_title");
		title.setLayoutResource(R.layout.setting_title);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// if (getString(R.string.setting_p2p).equals(preference.getKey())) {
		// AppConstants.USE_P2P = (Boolean) newValue;
		// } else {
		// AppConstants.SOFT_DECODE = (Boolean) newValue;
		// }
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
