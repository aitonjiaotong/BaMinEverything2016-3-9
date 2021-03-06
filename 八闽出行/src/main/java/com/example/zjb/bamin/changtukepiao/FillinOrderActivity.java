package com.example.zjb.bamin.changtukepiao;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.example.administrator.shane_library.shane.utils.GsonUtils;
import com.example.administrator.shane_library.shane.utils.HTTPUtils;
import com.example.administrator.shane_library.shane.utils.VolleyListener;
import com.example.administrator.shane_library.shane.widget.ListView4ScrollView;
import com.example.zjb.bamin.R;
import com.example.zjb.bamin.constant.Constant;
import com.example.zjb.bamin.models.about_order.OrderInfo;
import com.example.zjb.bamin.models.about_order.OrderList;
import com.example.zjb.bamin.models.about_ticket.TicketInfo;
import com.example.zjb.bamin.models.about_used_contact.UsedContactInfo;
import com.example.zjb.bamin.utils.DialogShow;
import com.example.zjb.bamin.utils.TimeAndDateFormate;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FillinOrderActivity extends Activity implements View.OnClickListener {

    private ImageView mBack;
    private TicketInfo mTicketInfo;
    private int ticketNumBuy = 0;
    private int ticketChildNum = 0;
    private ListView4ScrollView mPassager_list;
    private List<UsedContactInfo> mTicketPassagerList = new ArrayList<>();
    //是否有卖保险
    private boolean isInsure = false;
    /**
     * 广播刷新乘客列表
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "ticketPassager":
                    boolean isExit = false;
                    UsedContactInfo ticketPassager = (UsedContactInfo) intent.getSerializableExtra("ticketPassager");
                    for (int i = 0; i < mTicketPassagerList.size(); i++) {
                        if (ticketPassager.getIdcard().equals(mTicketPassagerList.get(i).getIdcard())) {
                            DialogShow.setDialog(FillinOrderActivity.this,"该乘客已存在", "确定");
                            isExit = true;
                        }
                    }
                    if (!isExit) {
                        ticketNumBuy = ticketNumBuy + 1;
                        mTicketPassagerList.add(ticketPassager);
                        mAdapter.notifyDataSetChanged();
                        refrashTicketNumAndPrice();
                    }
                    break;
            }
        }
    };
    private OrderInfo mOrderInfo;
    private PopupWindow mPopupWindow;
    private CheckBox mCheckBox_baoxian;
    private TextView mTextView_insure;

    private void refrashTicketNumAndPrice() {
        mPassager_count.setText(ticketNumBuy + "");
        mPassager_count_real.setText("共" + ticketNumBuy + "人乘车");
        mTotal_price.setText("￥" + ticketNumBuy * mTicketInfo.getFullPrice());
    }

    private MyAdapter mAdapter;
    private ImageView mAdd_passager;
    private TextView mPassager_count;
    private TextView mChild_num;
    private ImageView mChild_delete;
    private ImageView mChild_add;
    private TextView mPassager_count_real;
    private TextView mTotal_price;
    private String mPhoneNum;
    private String mId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fillin_order);
        /**
         * 获取用户id
         */
        SharedPreferences sp = getSharedPreferences("isLogin", Context.MODE_PRIVATE);
        mId = sp.getString("id", "");
        mPhoneNum = sp.getString("phoneNum", "");
        initIntent();
        initUI();
        initInsure();
        setOnclick();
    }

    //判断是否有卖保险
    private void initInsure() {
        int insurePrice = mTicketInfo.getInsurePrice();
        if (insurePrice > 0) {
            isInsure = true;
            mCheckBox_baoxian.setEnabled(true);
            mCheckBox_baoxian.setChecked(true);
            mTextView_insure.setText("保险");
            mTextView_insure.getPaint().setFlags(0);
        } else {
            isInsure = false;
            mCheckBox_baoxian.setEnabled(false);
            mCheckBox_baoxian.setChecked(false);
            mTextView_insure.setText("无保险");
            mTextView_insure.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void initIntent() {
        Intent intent = getIntent();
        mTicketInfo = (TicketInfo) intent.getSerializableExtra("ticketInfo");
    }

    private void initUI() {
        mTotal_price = (TextView) findViewById(R.id.total_price);
        mPassager_count_real = (TextView) findViewById(R.id.passager_Count_real);
        mChild_delete = (ImageView) findViewById(R.id.child_delete);
        mChild_add = (ImageView) findViewById(R.id.child_add);
        mChild_num = (TextView) findViewById(R.id.child_num);
        mPassager_count = (TextView) findViewById(R.id.passager_Count);
        mAdd_passager = (ImageView) findViewById(R.id.add_passager);
        mBack = (ImageView) findViewById(R.id.iv_back);
        TextView start_xianshi = (TextView) findViewById(R.id.start_xianshi);
        start_xianshi.setText(mTicketInfo.getStartSiteName());
        TextView end_xianshi = (TextView) findViewById(R.id.end_xianshi);
        end_xianshi.setText(mTicketInfo.getEndSiteName());
        TextView start_station = (TextView) findViewById(R.id.start_station);
        start_station.setText(mTicketInfo.getStationName());
        TextView end_station = (TextView) findViewById(R.id.end_station);
        end_station.setText(mTicketInfo.getEndZoneName());
        TextView coachGradeName = (TextView) findViewById(R.id.coachGradeName);
        coachGradeName.setText(mTicketInfo.getCoachGradeName());
        TextView ticket_price = (TextView) findViewById(R.id.ticket_price);
        ticket_price.setText("¥" + mTicketInfo.getFullPrice());
        TextView seatNum = (TextView) findViewById(R.id.seatNum);
        seatNum.setText("座位数:" + mTicketInfo.getCoachSeatNumber());
        TextView ticket_time = (TextView) findViewById(R.id.ticket_time);
        ticket_time.setText(TimeAndDateFormate.dateFormate(mTicketInfo.getExecuteDate()));
        TextView setoutTime = (TextView) findViewById(R.id.setoutTime);
        setoutTime.setText(TimeAndDateFormate.timeFormate(mTicketInfo.getSetoutTime()));
        mPassager_list = (ListView4ScrollView) findViewById(R.id.passager_list);
        mAdapter = new MyAdapter();
        mPassager_list.setAdapter(mAdapter);
        mChild_num.setText(ticketChildNum + "");
        //刷新票的价格
        refrashTicketNumAndPrice();
        mCheckBox_baoxian = (CheckBox) findViewById(R.id.checkBox_baoxian);
        mTextView_insure = (TextView) findViewById(R.id.textView_insure);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTicketPassagerList.size();
        }
        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View inflate = getLayoutInflater().inflate(R.layout.ticket_passager_item, null);
            TextView passager_name = (TextView) inflate.findViewById(R.id.passager_name);
            passager_name.setText(mTicketPassagerList.get(position).getName());
            inflate.findViewById(R.id.detete_contact).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ticketNumBuy = ticketNumBuy - 1;
                    mTicketPassagerList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    refrashTicketNumAndPrice();
                }
            });
            return inflate;
        }
    }

    private void setOnclick() {
        mAdd_passager.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mChild_delete.setOnClickListener(this);
        mChild_add.setOnClickListener(this);
        findViewById(R.id.order_commmit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            //提交订单
            case R.id.order_commmit:
                if (ticketNumBuy > 0) {
                    setPopupWindows();
                    orderCommit();
                } else {
                    DialogShow.setDialog(FillinOrderActivity.this,"请添加乘车人", "确定");
                }
                break;
            case R.id.child_delete:
                if (ticketChildNum > 0) {
                    ticketChildNum = ticketChildNum - 1;
                    mChild_num.setText(ticketChildNum + "");
                }
                break;
            case R.id.child_add:
                if (ticketNumBuy>0){
                    if (ticketChildNum<mTicketInfo.getCoachSeatNumber()/10){
                        ticketChildNum = ticketChildNum + 1;
                        mChild_num.setText(ticketChildNum + "");
                    }else{
                        DialogShow.setDialog(FillinOrderActivity.this, "班次携免票儿童童数已超规定比例","确认");
                    }
                }else{
                    DialogShow.setDialog(FillinOrderActivity.this,"请添加乘客","确认");
                }
                break;
            case R.id.add_passager:
                intent.putExtra("addContact", "FillinOrderActivity");
                intent.setClass(FillinOrderActivity.this, UsedContact.class);
                startActivity(intent);
                animFromSmallToBigIN();
                break;
            case R.id.iv_back:
                finish();
                AnimFromRightToLeftOUT();
                break;
        }
    }
    /**
     * 从小到大打开动画
     */
    private void animFromSmallToBigIN() {
        overridePendingTransition(R.anim.magnify_fade_in, R.anim.fade_out);
    }
    //弹出等待popupwindows，防止误操作
    private void setPopupWindows() {
        View inflate = getLayoutInflater().inflate(R.layout.popupmenu01, null);
        //最后一个参数为true，点击PopupWindow消失,宽必须为match，不然肯呢个会导致布局显示不完全
        mPopupWindow = new PopupWindow(inflate, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //设置外部点击无效
        mPopupWindow.setOutsideTouchable(false);
        //设置背景变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.7f;
        getWindow().setAttributes(lp);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
//        BitmapDrawable bitmapDrawable = new BitmapDrawable();
//        popupWindow.setBackgroundDrawable(bitmapDrawable);
        mPopupWindow.showAtLocation(inflate, Gravity.CENTER, 0, 0);
    }

    private void orderCommit() {
        String passengerIdentitys = "";
        String passengerNames = "";
        String passengerInfo = "";
        for (int i = 0; i < mTicketPassagerList.size(); i++) {
            passengerIdentitys += "&passengerIdentitys=" + mTicketPassagerList.get(i).getIdcard();
            passengerNames += "&passengerNames=" + URLEncoder.encode(mTicketPassagerList.get(i).getName());
        }
        passengerInfo = passengerIdentitys + passengerNames;
        String url_web = Constant.URL.HOST +
                "SellTicket_NoBill_Booking?scheduleCompanyCode=" + "YongAn" +
                "&executeScheduleID=" + mTicketInfo.getExecuteScheduleID() +
                "&startSiteID=" + mTicketInfo.getStartSiteID() +
                "&endSiteID=" + mTicketInfo.getEndSiteID() +
                "&fullTickets=" + ticketNumBuy +
                "&halfTicket=" + "0" +
                "&carryChild=" + ticketChildNum +
                "&phoneNumber=" + mPhoneNum +
                passengerInfo +
                "&insured=" + mCheckBox_baoxian.isChecked();
        HTTPUtils.get(FillinOrderActivity.this, url_web, new VolleyListener() {
            public void onErrorResponse(VolleyError volleyError) {
                DialogShow.setDialog(FillinOrderActivity.this,"网络连接异常或正在维护","确认");
            }

            public void onResponse(String s) {
                Document doc = null;
                try {
                    doc = DocumentHelper.parseText(s);
                    Element testElement = doc.getRootElement();
                    String testxml = testElement.asXML();
                    String mResult;
                    mResult = testxml.substring(testxml.indexOf(">") + 1, testxml.lastIndexOf("<"));
                    mOrderInfo = GsonUtils.parseJSON(mResult, OrderInfo.class);
                    if (mOrderInfo.getBookLogAID() == null) {
                        if ("班次有关参数值错误，未能查询到对应班次".equals(mOrderInfo.getMessage())){
                            DialogShow.setDialog(FillinOrderActivity.this, "暂不支持三明地区以外的出发地", "确定");
                        }else{
                            DialogShow.setDialog(FillinOrderActivity.this, mOrderInfo.getMessage(), "确定");
                        }
                        mPopupWindow.dismiss();
                    } else {
                        /**
                         * 传订单号到艾通服务器
                         */
                        commitOrderToAiTon();
                    }

                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void commitOrderToAiTon() {
        String url = Constant.URLFromAiTon.HOST+"front/addorder";

        Map<String, String> map = new HashMap<>();
        map.put("bookLogAID",mOrderInfo.getBookLogAID());
        map.put("account_id", mId);
        map.put("redEnvelope_id", "");
        HTTPUtils.post(FillinOrderActivity.this, url, map, new VolleyListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                DialogShow.setDialog(FillinOrderActivity.this,"网络连接异常或正在维护","确认");
            }

            @Override
            public void onResponse(String s) {
                OrderList orderList = GsonUtils.parseJSON(s, OrderList.class);

                Intent intent = new Intent();
                intent.setClass(FillinOrderActivity.this, PayActivity.class);
                intent.putExtra("BookLogAID", mOrderInfo.getBookLogAID());
                intent.putExtra("OrderId", orderList.getId() + "");
                startActivity(intent);
                animFromSmallToBigIN();
                mPopupWindow.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ticketPassager");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    /**
     * 从右往左结束动画
     */
    private void AnimFromRightToLeftOUT() {
        overridePendingTransition(R.anim.fade_in, R.anim.push_left_out);
    }

    //重写back方法
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
            AnimFromRightToLeftOUT();
        }
        return super.onKeyDown(keyCode, event);
    }
}
