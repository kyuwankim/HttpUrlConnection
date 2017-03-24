package com.kyuwankim.android.httpurlconnection;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    Button btnGet;
    EditText editUrl;
    TextView txtResult, txtTitle;
    String title = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGet = (Button) findViewById(R.id.btnGet);
        editUrl = (EditText) findViewById(R.id.editUrl);
        txtResult = (TextView) findViewById(R.id.textResult);
        txtTitle = (TextView) findViewById(R.id.texttitle);

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlString = editUrl.getText().toString();
                getUrl(urlString);
            }
        });
    }

    public void getUrl(String urlString){

        if(!urlString.startsWith("http")){
            urlString = "http://"+urlString;
        }

        new AsyncTask<String, Void, String>(){

            ProgressDialog dialog = new ProgressDialog(MainActivity.this);


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Loading");

                dialog.show();
            }

            @Override
            protected String doInBackground(String... params) {
                String urlString = params[0];
                try {
                    // 1. String 을 url 객체로 변환
                    URL url = new URL(urlString);
                    // 2. url 로 네트워크 연결시작
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    // 3. url 연결에 대한 옵션 설정
                    connection.setRequestMethod("GET"); // 가.GET  : 데이터 요청시 사용하는 방식
                    // 나.POST : 데이터 입력시
                    // 다.PUT  : 데이터 수정시
                    // 라.DELETE : 데이터 삭제시
                    // 4. 서버로부터 응답코드 회신
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // 4.1 서버연결로 부터 스트림을 얻고, 버퍼래퍼로 감싼다
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        // 4.2 반복문을 돌면서 버퍼의 데이터를 읽어온다
                        StringBuilder result = new StringBuilder();
                        String lineOfData = "";
                        int start=0, end=0;
                        while ((lineOfData = br.readLine()) != null) {

                            result.append(lineOfData);

                        }
                        start = result.indexOf("<title>");
                        end = result.indexOf("</title>");
                        title = result.substring(start+7,end);

                        return result.toString();

                    } else {
                        Log.e("HTTPConnection", "Error Code=" + responseCode);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                // 결과값 메인 UI 에 세팅
                txtResult.setText(result);
                txtTitle.setText(title);

                dialog.dismiss();
            }

        }.execute(urlString);


    }
}