package ca.xahive.app.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ca.xahive.app.bl.local.Model;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Model.getInstance().setShareBundle(intent.getExtras());

        Intent mainIntent = new Intent(getApplicationContext(), TabBarManagerActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(mainIntent);
    }

}
