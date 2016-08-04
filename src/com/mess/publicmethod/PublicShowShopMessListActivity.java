package com.mess.publicmethod;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.mess.clientdlnu.R;
import com.mess.ordermess.adapter.NewsActivityAdapter;
import com.mess.ordermess.constant.Constants;
import com.mess.ordermess.dao.MessMenu;
import com.mess.ordermess.net.NetUntils;

public class PublicShowShopMessListActivity extends Activity {

	private LinearLayout shop_mess_Loading;
	private GridView lv_shop_mess;
	private String shop_Id;
	private NewsActivityAdapter adapter;
	private List<MessMenu> infos;
	private TextView tv_nav_title;

	private int firstX;
	private int firstY;

	private int maxTemp = 15;
	private int offset = 0;
	private int maxNumber = maxTemp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_navigation_mess_activity);

		this.lv_shop_mess = (GridView) this.findViewById(R.id.lv_shop_mess);
		this.shop_mess_Loading = (LinearLayout) this
				.findViewById(R.id.shop_mess_Loading);
		tv_nav_title = (TextView) this.findViewById(R.id.tv_nav_title);

		tv_nav_title.setText("��Ҳ���");
		infos = new ArrayList<MessMenu>();
		receiveData();

		fillShopData();
		addListener();

	}

	/**
	 * ������һ��ҳ��
	 */
	public void navigationBack(View view) {
		this.finish();
	}

	/**
	 * ����listView�ļ����¼�
	 */
	private void addListener() {
		lv_shop_mess.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getApplicationContext(),
						PublicShowMessMsgActivity.class);
				intent.putExtra("com.mess.ordermess.dao.MessMenu",
						(Parcelable) infos.get(position));
				startActivity(intent);
			}

		});

		lv_shop_mess.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:// ����״̬
					int lastPos = lv_shop_mess.getLastVisiblePosition();
					if (NetUntils
							.isNetworkAvailable(PublicShowShopMessListActivity.this)) {
						if (lastPos == (infos.size() - 1)) {
							offset++;
							fillShopData();
							shop_mess_Loading.setVisibility(View.VISIBLE);
						}
					} else {
						Toast.makeText(PublicShowShopMessListActivity.this,
								"��������ʧ�ܣ�", 0).show();
					}
					break;
				case OnScrollListener.SCROLL_STATE_FLING:// ���Ի���״̬
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// ��ָ����״̬
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

		});

		this.lv_shop_mess.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				boolean result = false;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					firstX = (int) event.getX();
					firstY = (int) event.getY();
					break;
				case MotionEvent.ACTION_UP:
					int distanceX = (int) (firstX - event.getX());
					int distanceY = (int) Math.abs(firstY - event.getY());
					if (distanceY > Math.abs(distanceX))
						return false;
					if (distanceX < -10) {
						PublicShowShopMessListActivity.this.finish();
						result = true;
					} else {
						result = false;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				}
				return result;
			}

		});
	}

	/**
	 * �õ�OrderMessActivity����������
	 */
	public void receiveData() {
		Intent intent = this.getIntent();
		shop_Id = intent.getStringExtra("com.dlnu.ordermess.dao.ShopMsgInfo");
	}

	/**
	 * �������
	 */
	private void fillShopData() {
		shop_mess_Loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				getMessMessage(offset, maxNumber);
			}
		}.start();
	}

	public void getMessMessage(int offset, int maxNumber) {
		final BmobQuery<MessMenu> query = new BmobQuery<MessMenu>();
		query.addWhereEqualTo("shop_Id", shop_Id);
		query.setLimit(maxNumber);
		query.setSkip(maxNumber * offset);
		query.findObjects(PublicShowShopMessListActivity.this,
				new FindListener<MessMenu>() {

					@Override
					public void onError(int arg0, String arg1) {
						shop_mess_Loading.setVisibility(View.INVISIBLE);
						Toast.makeText(PublicShowShopMessListActivity.this,
								"��ȡ����ʧ��!", 0).show();
					}

					@Override
					public void onSuccess(List<MessMenu> messMenu) {
						if (messMenu.size() > 0) {
							if (infos == null) {
								infos = messMenu;
							} else {
								infos.addAll(messMenu);
							}

							if (adapter == null) {
								adapter = new NewsActivityAdapter(
										PublicShowShopMessListActivity.this,
										Constants.GRIDVIEW_CLASSIFY_ADAPTER,
										infos);
								lv_shop_mess.setAdapter(adapter);
							} else {
								adapter.notifyDataSetChanged();
							}
						} else {
							Toast.makeText(PublicShowShopMessListActivity.this,
									"û�и���������!", 0).show();
						}
						shop_mess_Loading.setVisibility(View.INVISIBLE);
					}
				});
	}

}