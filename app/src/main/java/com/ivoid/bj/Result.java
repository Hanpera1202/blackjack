package com.ivoid.bj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nakazato on 2015/10/27.
 */
public class Result extends Activity {

    Game game;

    private final String getResultsUrl = "http://blackjack.uh-oh.jp/results/%s";

    private List<String> ids = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private List<String> imageUrls = new ArrayList<String>();
    private List<Integer> winNums = new ArrayList<Integer>();
    private List<String> endDates = new ArrayList<String>();
    private List<Integer> totalApplicationNums = new ArrayList<Integer>();
    private List<Integer> applicationNums = new ArrayList<Integer>();
    private List<String> progresses = new ArrayList<String>();
    private List<String> results = new ArrayList<String>();
    private BaseAdapter adapter;

    private Player player;

    AsyncJsonLoader asyncJsonLoader;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        setContentView(R.layout.result);

        player = new Player(getApplicationContext(), "God");
        game.setHeaderData(player, (RelativeLayout) findViewById(R.id.header));

        asyncJsonLoader = new AsyncJsonLoader(this, new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    // 各 ATND イベントのタイトルを配列へ格納
                    JSONArray resultArray = result.getJSONArray("results");
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject competition = resultArray.getJSONObject(i);
                        ids.add(competition.getString("id"));
                        names.add(competition.getString("name"));
                        imageUrls.add(competition.getString("image_url"));
                        winNums.add(competition.getInt("win_num"));
                        endDates.add(competition.getString("end_date"));
                        totalApplicationNums.add(competition.getInt("total_application_num"));
                        applicationNums.add(competition.getInt("application_num"));
                        progresses.add(competition.getString("progress"));
                        results.add(competition.getString("result"));
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
        asyncJsonLoader.execute(String.format(getResultsUrl, game.getUserId()));
    }

    @Override
    public void onResume(){
        super.onResume();
        // Redisplay dialog
        if (asyncJsonLoader.isInProcess()) {
            asyncJsonLoader.showDialog();
        }
    }

    public void setAdapter(){
        if(ids.isEmpty()){
            ((TextView) findViewById(R.id.message)).setText("You do not yet apply.");
        }else {
            ((TextView) findViewById(R.id.message)).setText("Let's see the results!!");
            // ListViewのインスタンスを生成
            ListView listView = (ListView) findViewById(R.id.listView);

            // BaseAdapter を継承したadapterのインスタンスを生成
            // 子要素のレイアウトファイル competition_list_items.xml を activity_main.xml に inflate するためにadapterに引数として渡す
            adapter = new ListViewAdapter(this, R.layout.result_list_items);

            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }

        //listView.setOnItemClickListener(this);
    }

    class ViewHolder {
        TextView name;
        ImageView image;
        TextView winNum;
        TextView endDate;
        TextView totalApplicationNum;
        TextView applicationNum;
        TextView progress;
        Button checkResult;
    }

    class ListViewAdapter extends BaseAdapter implements View.OnClickListener{
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
                holder.totalApplicationNum = (TextView) convertView.findViewById(R.id.totalApplicationNum);
                holder.applicationNum = (TextView) convertView.findViewById(R.id.applicationNum);
                holder.progress = (TextView) convertView.findViewById(R.id.progress);
                holder.checkResult = (Button)convertView.findViewById(R.id.checkResult);
                holder.checkResult.setOnClickListener(this);
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
            if(winNums.get(position) > 1) {
                holder.winNum.setText("Total " + winNums.get(position) + " winners");
            }else{
                holder.winNum.setText("Total 1 winner");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = sdf.parse(endDates.get(position));
                SimpleDateFormat convertFormat = new SimpleDateFormat("HH:mm MM/dd/yyyy");
                convertFormat.setTimeZone(TimeZone.getDefault());
                holder.endDate.setText("Event Priod : Until " + convertFormat.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.totalApplicationNum.setText(String.valueOf(totalApplicationNums.get(position)));
            holder.applicationNum.setText("Your Application Number : " + applicationNums.get(position));

            switch(progresses.get(position)){
                case "1":
                    holder.progress.setText("Currently in applying...");
                    holder.checkResult.setVisibility(Button.GONE);
                    break;
                case "2":
                    holder.progress.setText("Currently in lottery...");
                    holder.checkResult.setVisibility(Button.GONE);
                    break;
                case "3":
                    holder.progress.setText("Currently in announcing.");
                    holder.checkResult.setVisibility(Button.VISIBLE);
                    break;
            }

            holder.checkResult.setTag(ids.get(position));
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
            String competition_id = (String) view.getTag();
            Intent intent = new Intent(getApplicationContext(), ResultDialog.class);
            intent.putExtra("competition_id", competition_id);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }

    }

    public void onClickHeader(final View view) {
        startActivity(game.getNewIntent(view));
        finish();
        overridePendingTransition(0, 0);
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
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
