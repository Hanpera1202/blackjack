package com.ivoid.bj;

import com.bj.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nakazato on 2015/10/27.
 */
public class Competition extends Activity implements View.OnClickListener {

    private final String getActiveUrl = "http://blackjack.uh-oh.jp/active/%s";
    private final String applyUrl = "http://blackjack.uh-oh.jp/apply/%s/%s";

    private List<String> ids = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private List<String> imageUrls = new ArrayList<String>();
    private List<String> winNums = new ArrayList<String>();
    private List<String> endDates = new ArrayList<String>();
    private List<String> applyNums = new ArrayList<String>();
    private List<String> points = new ArrayList<String>();
    private List<String> myApplyNums = new ArrayList<String>();
    private BaseAdapter adapter;

    private AlertDialog alertDialog;

    private Player player;
    private TextView playerCash;

    private int waittime;
    private final Handler handler = new Handler();

    private SharedPreferences preference;
    private String user_id;

    AsyncJsonLoader asyncJsonLoader;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.competition);

        findViewById(R.id.game).setOnClickListener(this);
        findViewById(R.id.result).setOnClickListener(this);
        findViewById(R.id.checkMyData).setOnClickListener(this);

        //プリファレンスの準備
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        user_id = preference.getString("user_id", "");

        player = new Player(getApplicationContext(), "Richard");
        playerCash=(TextView)findViewById(R.id.playerCash);
        playerCash.setText(String.valueOf((int) player.getBalance()));

        asyncJsonLoader = new AsyncJsonLoader(this, new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    // 各 ATND イベントのタイトルを配列へ格納
                    JSONArray competitionArray = result.getJSONArray("competitions");
                    for (int i = 0; i < competitionArray.length(); i++) {
                        JSONObject competition = competitionArray.getJSONObject(i);
                        ids.add(competition.getString("id"));
                        names.add(competition.getString("name"));
                        imageUrls.add(competition.getString("image_url"));
                        winNums.add(competition.getString("win_num"));
                        endDates.add(competition.getString("end_date"));
                        applyNums.add(competition.getString("apply_num"));
                        myApplyNums.add(competition.getString("my_apply_num"));
                        points.add(competition.getString("point"));
                    }
                    setAdapter();

                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });
        // 処理を実行
        asyncJsonLoader.execute(String.format(getActiveUrl, user_id));
    }

    @Override
    public void onResume(){
        super.onResume();
        // ダイアログの再表示
        if (asyncJsonLoader.isInProcess()) {
            asyncJsonLoader.showDialog();
        }
    }

    public void setAdapter(){
        // ListViewのインスタンスを生成
        ListView listView = (ListView)findViewById(R.id.listView);

        // BaseAdapter を継承したadapterのインスタンスを生成
        // 子要素のレイアウトファイル competition_list_items.xml を activity_main.xml に inflate するためにadapterに引数として渡す
        adapter = new ListViewAdapter(getApplicationContext(),R.layout.competition_list_items);

        // ListViewにadapterをセット
        listView.setAdapter(adapter);

        //listView.setOnItemClickListener(this);
    }

    class ViewHolder {
        TextView name;
        ImageView image;
        TextView winNum;
        TextView endDate;
        TextView applyNum;
        TextView myApplyNum;
        TextView point;
        Button apply;
    }

    class ListViewAdapter extends BaseAdapter implements View.OnClickListener {
        private LayoutInflater inflater;
        private int itemLayoutId;

        public ListViewAdapter(Context context, int itemLayoutId) {
            super();
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            // 最初だけ View を inflate して、それを再利用する
            if (convertView == null) {
                // activity_main.xml の ＜ListView .../＞ に list_items.xml を inflate して convertView とする
                convertView = inflater.inflate(itemLayoutId, parent, false);
                // ViewHolder を生成
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.winNum = (TextView) convertView.findViewById(R.id.winNum);
                holder.endDate = (TextView) convertView.findViewById(R.id.endDate);
                holder.applyNum = (TextView) convertView.findViewById(R.id.applyNum);
                holder.myApplyNum = (TextView) convertView.findViewById(R.id.myApplyNum);
                holder.point = (TextView) convertView.findViewById(R.id.point);
                holder.apply = (Button)convertView.findViewById(R.id.apply);
                holder.apply.setOnClickListener(this);
                convertView.setTag(holder);
            }
            // holder を使って再利用
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(names.get(position));
            holder.image.setTag(imageUrls.get(position));
            AsyncImageLoader asyncImageLoader = new AsyncImageLoader(holder.image);
            asyncImageLoader.execute(imageUrls.get(position));
            holder.winNum.setText("Total " + winNums.get(position) + " winners");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = sdf.parse(endDates.get(position));
                SimpleDateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                convertFormat.setTimeZone(TimeZone.getDefault());
                holder.endDate.setText("Closing Time:" + convertFormat.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.applyNum.setText("Apply Num:" + applyNums.get(position));
            holder.myApplyNum.setText("My Apply Num:" + myApplyNums.get(position));
            holder.point.setText("Use Point:" + points.get(position));
            holder.apply.setTag(ids.get(position));

            return convertView;
        }

        @Override
        public int getCount() {
            // items の全要素数を返す
            return names.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * リスト内のボタンがクリックされたら呼ばれる
         */
        public void onClick(View view) {
            String applicaiton_id = (String) view.getTag();
            applyCompetition(applicaiton_id);
        }
    }

    void applyCompetition(String applicaiton_id){
        Integer point = Integer.parseInt(points.get(ids.indexOf(applicaiton_id)));
        AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(this,new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    String decrease_point = result.getString("decrease_point");
                    if(!decrease_point.equals("false")) {
                        player.withdraw(Integer.parseInt(decrease_point));
                        updatePlayerCashlbl();
                        alertDialog("Applicants completed");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        },"Applying");
        // 処理を実行
        if(player.getBalance() >= point) {
            asyncJsonLoader.execute(String.format(applyUrl, user_id, applicaiton_id));
        }else{
            alertDialog("Your point is not enough");
        }
    }

    int countUpNum;
    void updatePlayerCashlbl()
    {
        int beforeCash = Integer.parseInt(String.valueOf(playerCash.getText()));
        int afterCash = (int) player.getBalance();
        int loopCnt;
        int countCash;
        int sign = 1;
        if(afterCash - beforeCash < 0){
            sign = -1;
        }

        if((afterCash - beforeCash) * sign < 10){
            countCash = sign;
            loopCnt = (afterCash - beforeCash) * sign;
        }else{
            countCash = (afterCash - beforeCash) / 10;
            loopCnt = 10;
        }
        for (int i = 1; i <= loopCnt; i++) {
            if (i == loopCnt) {
                countUpNum = (int) player.getBalance();
            } else {
                countUpNum = beforeCash + (countCash * i);
            }
            handler.postDelayed(new Runnable() {
                int updateCash = countUpNum;
                public void run() {
                    playerCash.setText(String.valueOf(updateCash));
                }
            }, waittime + (i * 50));
        }
    }

    // アラートダイアログ表示
    private void alertDialog(String message) {
        AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(this);
        AlertDialogBuilder.setMessage(message)
                          .setPositiveButton("OK", null);
        alertDialog = AlertDialogBuilder.create();
        alertDialog.show();

    }

    // アラートダイアログ非表示
    private void dismissAalertDialog() {
        if (alertDialog !=  null) {
            alertDialog.dismiss();
        }
        alertDialog = null;
    }

    // 応募完了メッセージ表示
    private void showApplyComplete() {
        Toast toast = Toast.makeText(this, "Applicants completed", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void onClick(final View view)
    {
        switch (view.getId()) {
            case R.id.game: {
                Intent intent = new Intent(this, Dealer.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.result: {
                Intent intent = new Intent(this, Result.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.checkMyData: {
                Intent intent = new Intent(this, MyData.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return false;
    }

    @Override
    public void onUserLeaveHint() {
        if (asyncJsonLoader != null) {
            // ダイアログを閉じる（2重表示防止）
            asyncJsonLoader.dismissDialog();
            dismissAalertDialog();
        }
    }
}
