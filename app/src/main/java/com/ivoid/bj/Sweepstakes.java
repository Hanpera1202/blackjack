package com.ivoid.bj;

import com.bj.R;
import com.google.android.gms.ads.AdListener;

import android.content.Context;
import android.os.Bundle;

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
 * Created on 2015/10/27.
 */
public class Sweepstakes extends FragmentActivity {

    Game game;

    private final String getActiveUrl = "http://sweepstakes.uh-oh.jp/sweepstakes";
    private final String entryUrl = "http://sweepstakes.uh-oh.jp/users/%s/entry";

    private List<String> ids = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private List<String> imageUrls = new ArrayList<String>();
    private List<Integer> winNums = new ArrayList<Integer>();
    private List<String> endDates = new ArrayList<String>();
    private List<Integer> totalEntryNums = new ArrayList<Integer>();
    private List<Integer> entryNums = new ArrayList<Integer>();
    private BaseAdapter adapter;

    private DialogFragment alertDialog;
    private ConfirmDialogFragment ConfirmDialog;

    private Player player;

    AsyncJsonLoader asyncJsonLoader;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        setContentView(R.layout.sweepstakes);

        player = new Player(getApplicationContext(), "God");
        game.setHeaderData(player,(RelativeLayout)findViewById(R.id.header));

        asyncJsonLoader = new AsyncJsonLoader(this, new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    // 各 ATND イベントのタイトルを配列へ格納
                    JSONArray competitionArray = result.getJSONArray("sweepstakes");
                    for (int i = 0; i < competitionArray.length(); i++) {
                        JSONObject competition = competitionArray.getJSONObject(i);
                        ids.add(competition.getString("id"));
                        names.add(competition.getString("name"));
                        imageUrls.add(competition.getString("image_url"));
                        winNums.add(competition.getInt("win_num"));
                        endDates.add(competition.getString("end_date"));
                        totalEntryNums.add(competition.getInt("total_entry_num"));
                        entryNums.add(competition.getInt("entry_num"));
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
            ((TextView) findViewById(R.id.message)).setText("Let's enter a sweepstakes!!");
            // ListViewのインスタンスを生成
            ListView listView = (ListView) findViewById(R.id.listView);

            // BaseAdapter を継承したadapterのインスタンスを生成
            // 子要素のレイアウトファイル competition_list_items.xml を activity_main.xml に inflate するためにadapterに引数として渡す
            adapter = new ListViewAdapter(this, R.layout.sweepstakes_list_items);

            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
        setTicketNumber();
    }

    public void setTicketNumber(){
        String ticketMessage;
        String ticketNum;
        String ticketUnit;
        if(player.getTicketBalance() > 0) {
            ticketMessage = "You have ";
            ticketNum = String.valueOf(player.getTicketBalance());
            if (player.getTicketBalance() == 1) {
                ticketUnit = " ticket.";
            } else {
                ticketUnit = " tickets.";
            }
        }else {
            ticketMessage = "You don't have a ticket.";
            ticketNum = "";
            ticketUnit = "";
        }
        ((TextView) findViewById(R.id.ticketMessage)).setText(ticketMessage);
        ((TextView) findViewById(R.id.ticketNum)).setText(ticketNum);
        ((TextView) findViewById(R.id.ticketUnit)).setText(ticketUnit);
    }

    class ViewHolder {
        TextView name;
        ImageView image;
        TextView winNum;
        TextView endDate;
        TextView totalEntryNum;
        TextView entryNum;
        Button entry;
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
                holder.totalEntryNum = (TextView) convertView.findViewById(R.id.totalEntryNum);
                holder.entryNum = (TextView) convertView.findViewById(R.id.entryNum);
                holder.entry = (Button)convertView.findViewById(R.id.entry);
                holder.entry.setOnClickListener(this);
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
            holder.totalEntryNum.setText(String.valueOf(totalEntryNums.get(position)));
            holder.entryNum.setText("Your Entry Number : " + entryNums.get(position));
            holder.entry.setTag(ids.get(position));

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
            if(player.getTicketBalance() >= 1) {
                String message = "Are you sure you want to enter using a ticket?";
                createConfirmDialog("confirmEntryDialog", message, competitionId);
                showConfirmDialog();
            }else{
                createAlertDialog("default", "You don't have a ticket.");
                showAlertDialog();
            }
        }
    }

    void entrySweepstakes(String sweepstakesId){
        AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(this,new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    if(result.getBoolean("result")) {
                        String entrySweepstakesId = result.getString("sweepstakes_id");
                        Integer index = ids.indexOf(entrySweepstakesId);
                        player.withdrawTicket(1);
                        totalEntryNums.set(index, totalEntryNums.get(index) + 1);
                        entryNums.set(index, entryNums.get(index) + 1);
                        adapter.notifyDataSetChanged();
                        setTicketNumber();
                        showAlertDialog();
                    }else{
                        String reason = result.getString("reason");
                        switch(reason){
                            case "ENDED":
                                updateAlertDialogArgs("default", "This sweepstakes has ended.");
                                break;
                            default:
                                updateAlertDialogArgs("default", "Enter failed.");
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
        },"Entering");
        // 処理を実行
        createAlertDialog("entryCompletedDialog", "Entry completed.");
        String url = String.format(entryUrl, game.getUserId());
        try {
            Crypt crypt = new Crypt("sweepstakes_id=" + sweepstakesId + "&timestamp=" + System.currentTimeMillis()/1000);
            asyncJsonLoader.execute("POST", url, "entry_data", crypt.encrypt());
        } catch (Exception e) {
            e.printStackTrace();
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
