package assignment.fragment.rsantosh.com.imagehandler;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mURL;
    private Button mDownload;
    private ImageView mImageView;
    private ProgressDialog mProgressDialog;
    private String mUrl;
    private Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mURL = (EditText) findViewById(R.id.etUrl);
        mDownload = (Button) findViewById(R.id.btnDownload);
        mImageView = (ImageView) findViewById(R.id.ivImage);
        mURL.setText("http://kingofwallpapers.com/tiger/tiger-013.jpg");
        mDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(checkInternetConnection()) {
            // progressDialog = ProgressDialog.show(MainActivity.this, "", "Downloading..Please wait..");
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage("Downloading..please wait..");
            mProgressDialog.setMax(100);
            mProgressDialog.show();
            new Thread() {
                public void run() {
                    mUrl = mURL.getText().toString();


                    mBitmap = downloadBitmap(mUrl);
                    messageHandler.sendEmptyMessage(0);

                    // save image in sd card inside saved_image folder
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/saved_images");
                    myDir.mkdirs();
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);
                    String fname = "Image-" + n + ".jpg";
                    File file = new File(myDir, fname);
                    if (file.exists()) file.delete();

                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        else
        {

            AlertDialog.Builder dialog=new AlertDialog.Builder(this);
            dialog.setIcon(android.R.drawable.presence_offline);
            dialog.setTitle("Network Connection");
            dialog.setMessage("Oops! No internet connection..");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mImageView.setImageBitmap(mBitmap);
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    };


    private Bitmap downloadBitmap(String url) {
        int count=0;
        InputStream is = null;
        BufferedInputStream bis = null;
        Bitmap bmp = null;
        URL link = null;
        try {
            link = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            URLConnection conn = link.openConnection();
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is);
            bmp = BitmapFactory.decodeStream(bis);
            int lengthOfFile=conn.getContentLength();
            byte data[] = new byte[1024];

            long total = 0;

            InputStream inputStream=new BufferedInputStream(link.openStream(),8192);
            while ((count = inputStream.read(data)) != -1) {
                total += count;
                //update the progress bar
                mProgressDialog.setProgress((int)((total*100)/lengthOfFile));

            }
        } catch (MalformedURLException e) {

        } catch (IOException e) {

        } finally {
            try {
                if (is != null)
                    is.close();
                if (bis != null)
                    bis.close();
            } catch (IOException e) {

            }
        }

        return bmp;


    }
    //check the internet connection
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connect =(ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork=connect.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }


}