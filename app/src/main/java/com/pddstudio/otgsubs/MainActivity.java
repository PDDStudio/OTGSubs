package com.pddstudio.otgsubs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pddstudio.substratum.packager.SubstratumPackager;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new SubstratumPackager.Builder(this).build().doWork();
	}
}
