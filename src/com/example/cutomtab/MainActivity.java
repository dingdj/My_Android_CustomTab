package com.example.cutomtab;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		CustomViewTab tab = (CustomViewTab)findViewById(R.id.tab);
		tab.addTitle(new String[]{"111", "222", "333"});
		
		CustomViewPager pager = (CustomViewPager)findViewById(R.id.pager);
		LayoutInflater l = LayoutInflater.from(this);
		TextView v1 = (TextView) l.inflate(R.layout.tab_content, null);
		TextView v2 = (TextView) l.inflate(R.layout.tab_content, null);
		v2.setText("content2");
		TextView v3 = (TextView) l.inflate(R.layout.tab_content, null);
		v3.setText("content3");
		pager.addView(v1);
		pager.addView(v2);
		pager.addView(v3);
		pager.setTab(tab);
		
		tab.setPager(pager);
	}
}
