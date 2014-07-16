package edu.cicese.android.ans;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;
import com.android.ans.R;

public class Preferences extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    Utilities.menuClicked = true;
        addPreferencesFromResource(R.xml.preferences);
	    setContentView(R.layout.preflayout);
        
        Preference serverPref = findPreference("serverPref");
        serverPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Utilities.SERVER_ADDR = newValue.toString();
				ANS.changeServer(newValue.toString());
				return true;
			}
        });

	    Button btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnUpdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Server.uploadRepository();
			}
		});
        
        
        // Get the custom preference
//        Preference customPref = (Preference) findPreference("customPref");
//        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                    Toast.makeText(getBaseContext(),
//                                    "The custom preference has been clicked",
//                                    Toast.LENGTH_LONG).show();
//                    SharedPreferences customSharedPreference = getSharedPreferences(
//                                    "myCustomSharedPrefs", Activity.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = customSharedPreference
//                                    .edit();
//                    editor.putString("myCustomPref",
//                                    "The preference has been clicked");
//                    editor.commit();
//                    return true;
//            }
//
//        });
    }
}