package com.ivoid.bj;

import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class AsyncImageLoader extends AsyncTask<String, Integer, Bitmap> {

    private ImageView image;
    private String tag;

    public AsyncImageLoader(ImageView _image) {
        image = _image;
        tag = image.getTag().toString();
    }

    // バックグラウンドで処理する（重い処理）
    @Override
    protected Bitmap doInBackground(String... params) {
        String uri = params[0];
        return downloadImage(uri);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... _progress) {
        super.onProgressUpdate(_progress);
    }

    // バックグラウンド処理が終了した後にメインスレッドに渡す処理
    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        // Tagが同じものが確認して、同じであれば画像を設定する
        if (tag.equals(image.getTag())) {
            image.setImageBitmap(result);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    // 画像をダウンロード
    private Bitmap downloadImage(String uri) {
        Bitmap image = ImageCache.getImage(uri);
        if (image == null) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(uri);
            try {
                HttpResponse resp = httpClient.execute(httpGet);
                if (resp.getStatusLine().getStatusCode() < 400) {
                    InputStream is = resp.getEntity().getContent();
                    image = BitmapFactory.decodeStream(is);
                    ImageCache.setImage(uri, image);
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return image;
    }

}