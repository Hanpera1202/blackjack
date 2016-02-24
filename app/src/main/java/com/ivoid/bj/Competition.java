package com.ivoid.bj;

import com.bj.R;
import com.google.android.gms.ads.AdListener;

import android.content.Context;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nakazato on 2015/10/27.
 */
public class Competition extends FragmentActivity {

    Game game;

    private final String getActiveUrl = "http://blackjack.uh-oh.jp/competitions";
    private final String applyUrl = "http://blackjack.uh-oh.jp/users/%s/application";

    private List<String> ids = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private List<String> imageUrls = new ArrayList<String>();
    private List<Integer> winNums = new ArrayList<Integer>();
    private List<String> endDates = new ArrayList<String>();
    private List<Integer> totalApplicationNums = new ArrayList<Integer>();
    private List<Integer> applicationNums = new ArrayList<Integer>();
    private List<Integer> points = new ArrayList<Integer>();
    private BaseAdapter adapter;

    private DialogFragment alertDialog;
    private ConfirmDialogFragment ConfirmDialog;

    private Player player;
    private TextView playerCash;

    private final Handler handler = new Handler();

    AsyncJsonLoader asyncJsonLoader;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        setContentView(R.layout.competition);

        player = new Player(getApplicationContext(), "God");
        game.setHeaderData(player,(RelativeLayout)findViewById(R.id.header));

        playerCash=(TextView)findViewById(R.id.playerCash);

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
                        winNums.add(competition.getInt("win_num"));
                        endDates.add(competition.getString("end_date"));
                        totalApplicationNums.add(competition.getInt("total_application_num"));
                        applicationNums.add(competition.getInt("application_num"));
                        points.add(competition.getInt("point"));
                    }
                    setAdapter();

                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            // error
            public boolean postError() {
                return false;
            }
        });
        // 処理を実行
        asyncJsonLoader.execute("GET", getActiveUrl + "?user_unique_id=" + game.getUserId());
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
        if(ids.isEmpty()) {
            ((TextView) findViewById(R.id.message)).setText("Coming soon.");
        }else{
            ((TextView) findViewById(R.id.message)).setText("Let's apply to PRIZE COMPETITION!!");
            // ListViewのインスタンスを生成
            ListView listView = (ListView) findViewById(R.id.listView);

            // BaseAdapter を継承したadapterのインスタンスを生成
            // 子要素のレイアウトファイル competition_list_items.xml を activity_main.xml に inflate するためにadapterに引数として渡す
            adapter = new ListViewAdapter(this, R.layout.competition_list_items);

            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
    }

    class ViewHolder {
        TextView name;
        ImageView image;
        TextView winNum;
        TextView endDate;
        TextView totalApplicationNum;
        TextView applicationNum;
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
                holder.totalApplicationNum = (TextView) convertView.findViewById(R.id.totalApplicationNum);
                holder.applicationNum = (TextView) convertView.findViewById(R.id.applicationNum);
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
            holder.point.setText("Use Point : " + points.get(position));
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
            String competitionId = (String) view.getTag();
            if(player.getBalance() >= points.get(ids.indexOf(competitionId))) {
                Integer point = points.get(ids.indexOf(competitionId));
                String message = "Are you sure you want to apply using the " + point + "pt?";
                createConfirmDialog("confirmApplyDialog", message, competitionId);
                showConfirmDialog();
            }else{
                createAlertDialog("default", "Your point is not enough");
                showAlertDialog();
            }
        }
    }

    void applyCompetition(String competitionId){
        AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(this,new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    if(result.getBoolean("result")) {
                        String applyCompetitionId = result.getString("competition_id");
                        Integer index = ids.indexOf(applyCompetitionId);
                        player.withdraw(points.get(index));
                        totalApplicationNums.set(index, totalApplicationNums.get(index) + 1);
                        applicationNums.set(index, applicationNums.get(index) + 1);
                        adapter.notifyDataSetChanged();
                        updatePlayerCashlbl();
                        showAlertDialog();
                    }else{
                        String reason = result.getString("reason");
                        switch(reason){
                            case "ENDED":
                                updateAlertDialogArgs("default", "This prize competition has ended.");
                                break;
                            default:
                                updateAlertDialogArgs("default", "Applicantion failed.");
                        }
                        showAlertDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            // error
            public boolean postError() {
                return false;
            }
        },"Applying");
        // 処理を実行
        createAlertDialog("applyCompletedDialog", "Applicantion completed.");
        String url = String.format(applyUrl, game.getUserId());
        try {
            Crypt crypt = new Crypt("competition_id=" + competitionId + "&timestamp=" + System.currentTimeMillis()/1000);
            asyncJsonLoader.execute("POST", url, "apply_data", crypt.encrypt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int countUpNum;
    void updatePlayerCashlbl()
    {
        int waittime = 0;
        int beforeCash = Integer.parseInt(String.valueOf(playerCash.getText()));
        int afterCash = player.getBalance();
        int loopCnt;
        int countCash;
        int sign = 1;
        if(afterCash - beforeCash < 0){
            sign = -1;
        }

        if((afterCash - beforeCash) * sign < 30){
            countCash = sign;
            loopCnt = (afterCash - beforeCash) * sign;
        }else{
            countCash = (afterCash - beforeCash) / 30;
            loopCnt = 30;
        }
        for (int i = 1; i <= loopCnt; i++) {
            if (i == loopCnt) {
                countUpNum = player.getBalance();
            } else {
                countUpNum = beforeCash + (countCash * i);
            }
            handler.postDelayed(new Runnable() {
                int updateCash = countUpNum;
                public void run() {
                    playerCash.setText(String.valueOf(updateCash));
                }
            }, waittime + (i * 15));
        }
    }

    public void showAd(){
        if (game.mInterstitialAd.isLoaded()) {
            game.mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    game.requestNewInterstitial();
                }

                @Override
                public void onAdClosed() {
                    game.requestNewInterstitial();
                }
            });
            game.mInterstitialAd.show();
        }
    }

    // アラートダイアログ作成
    private void createAlertDialog(String dialogType, String message){
        alertDialog = AlertDialogFragment.newInstance(dialogType, message);
    }

    // アラートダイアログのメッセージを更新
    private void updateAlertDialogArgs(String dialogType, String message){
        if(alertDialog != null) {
            Bundle args = new Bundle();
            args.putString("dialogType", dialogType);
            args.putString("message", message);
            alertDialog.setArguments(args);
        }
    }

    // アラートダイアログ表示tag指定なし
    private void showAlertDialog() {
        if(alertDialog != null &&
                getSupportFragmentManager().findFragmentByTag("alertDialog") == null) {
            alertDialog.show(getSupportFragmentManager(), "alertDialog");
        }
    }

    // アラートダイアログ非表示
    private void dismissAlertDialog() {
        if (alertDialog !=  null) {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("alertDialog");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        }
        alertDialog = null;
    }

    // create confirm dialog
    private void createConfirmDialog(String dialogType, String message, String id){
        ConfirmDialog = ConfirmDialogFragment.newInstance(dialogType, message, id);
    }

    // show confirm dialog
    private void showConfirmDialog() {
        if (ConfirmDialog != null &&
                getSupportFragmentManager().findFragmentByTag("confirmDialog") == null) {
            ConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
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
            dismissAlertDialog();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
