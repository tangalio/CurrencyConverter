package com.example.currencyconverter;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.currencyconverter.adapter.CustomAdapter;
import com.example.currencyconverter.api.API;
import com.example.currencyconverter.api.RetrofitClient;
import com.example.currencyconverter.model.Geonames;
import com.example.currencyconverter.model.Item;
import com.example.currencyconverter.model.Suser;
import com.example.currencyconverter.utils.Utils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    TextInputLayout textinput;
    EditText editText;
    TextView txtview;
    Spinner sourcemoney, destinationmoney;
    Button button;
    private List<Item> itemList;
    private String mFeedDescription;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    API api;
    CustomAdapter customAdapter;
    String source, destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = RetrofitClient.getInstance(Utils.BASE_URL).create(API.class);
        Anhxa();
        dataSpinner();
        getData();
    }

    private void dataSpinner() {
        compositeDisposable.add(api.getdata()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        suser -> {
                            String g = new Gson().toJson(suser);
                            Suser suser1 = new Gson().fromJson(g, Suser.class);
                            ArrayList<Geonames> geonames = (ArrayList<Geonames>) suser1.getGeonames();

                            customAdapter = new CustomAdapter(getApplicationContext(), geonames);
                            sourcemoney.setAdapter(customAdapter);
                            destinationmoney.setAdapter(customAdapter);
                            sourcemoney.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    source = geonames.get(position).currencyCode;
                                    source = source.toLowerCase();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                            destinationmoney.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    destination = geonames.get(position).currencyCode;
                                    destination = destination.toLowerCase();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        }, throwable -> {
                            Toast.makeText(getApplicationContext(), "Không kết nối được với sever" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }


    private void Anhxa() {
        textinput = findViewById(R.id.textinput);
        editText = findViewById(R.id.editText);
        txtview = findViewById(R.id.txtview);
        sourcemoney = findViewById(R.id.sourcemoney);
        destinationmoney = findViewById(R.id.destinationmoney);
        button = findViewById(R.id.button);
    }

    private void getData() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchFeedTask().execute((Void) null);
            }
        });
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {
        private String urlLink;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            urlLink = "https://" + source + ".fxexchangerate.com/" + destination + ".xml";
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;
            try {
                if (!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;
                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                itemList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Bạn chưa nhập số tiền cần đổi", Toast.LENGTH_SHORT).show();
                } else {
                    double m = Double.parseDouble(editText.getText().toString());
                    double n = Double.parseDouble(mFeedDescription);
                    double s = m * n;
//                    DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
//                    decimalFormat.format(Double.parseDouble(String.valueOf(s)));
                    String a = String.valueOf(s);
                    txtview.setText(m + " " + source.toUpperCase() + " = " + a + " " + destination.toUpperCase());
                }
            } else {
                Toast.makeText(MainActivity.this,
                        "Hai nước trùng nhau!!! \n Vui lòng chọn nước khác",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<Item> parseFeed(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean isItem = false;
        List<Item> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();
                String name = xmlPullParser.getName();
                if (name == null)
                    continue;
                if (eventType == XmlPullParser.END_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }
                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }
                if (name.equalsIgnoreCase("title")) {
                    title = result;

                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                    mFeedDescription = description.replaceAll("<(.|\\n)*?>", "");
                    mFeedDescription = mFeedDescription.replaceAll("\\s", "");
                    mFeedDescription = mFeedDescription.substring(mFeedDescription.indexOf("=") + 1);
                    mFeedDescription = mFeedDescription.split(destination.toUpperCase())[0];
                }
            }
            return items;
        } finally {
            inputStream.close();
        }
    }
}