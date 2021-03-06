package thanachart.benz.masterbenzrestauranat;

import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    //Explicit
    private MyManage myManage;
    private EditText userEditText, passwordEditText;
    private String userString, passwordString;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind widget
        bindWidget();


        //Request SQLite
        myManage = new MyManage(this);

        //Test Add Value
        //testAddValue();

        //Delete All SQLite
        deleteAllSQLite();

        //Syn JSON to SQlite
        synJSONtoSQLite();

    } //Main Method

    public void clickLogin(View view) {
        userString = userEditText.getText().toString().trim();
        passwordString = passwordEditText.getText().toString().trim();

        //Check Space
        if (userString.equals("") || passwordString.equals("")) {
            //Have Space
            myAlert("มีช่องว่าง");

        } else {
            //No space
            checkUser();

        }

    }//clickLogin

    private void checkUser() {
        try {
            String[] myResultStrings = myManage.searchUser(userString);
            myAlert("Welcome "+ myResultStrings[3]);

        } catch (Exception e) {
            myAlert("ไม่มี" + userString +"ในฐานข้อมูลของเรา");
        }

    }//CheckUser

    private void myAlert(String strMessage) {
        Toast.makeText(MainActivity.this, strMessage, Toast.LENGTH_SHORT).show();
    }

    private void bindWidget() {
        userEditText = (EditText) findViewById(R.id.editText);
        passwordEditText = (EditText) findViewById(R.id.editText);
    }

    private void synJSONtoSQLite() {
        //Connected http://

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy
                .Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        int intTable =0;
        while (intTable <= 1) {
            
            //1 Create InputStream
            InputStream inputStream = null;
            String[] urlStrings = {"http://www.swiftcodingthai.com/19Mar/php_get_user_master.php",
                    "http://www.swiftcodingthai.com/19Mar/php_get_food_master.php"};
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(urlStrings[intTable]);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                inputStream = httpEntity.getContent();

            } catch (Exception e) {
                Log.d("Rest","InputStream ==> "+ e.toString());


            }

            //2 Create JSON String
            String strJSON = null;
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String strLine = null;
                while ((strLine = bufferedReader.readLine()) != null){
                    stringBuilder.append(strLine);
                } 
                inputStream.close();
                strJSON = stringBuilder.toString();
                
            } catch (Exception e) {
                Log.d("Reset", "JSON String ==>" + e.toString());
            }

            //3 Update to SQLite
            try {
                JSONArray jsonArray = new JSONArray(strJSON);
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    switch (intTable){
                        case 0:
                            String strUser = jsonObject.getString(MyManage.column_User);
                            String strPassword = jsonObject.getString(MyManage.column_Password);
                            String strName = jsonObject.getString(MyManage.column_Name);

                            myManage.addValue(1, strUser, strPassword, strName);

                            break;
                        case 1:
                            String strFood = jsonObject.getString(MyManage.column_Food);
                            String strPrice = jsonObject.getString(MyManage.column_Price);
                            String strSource =jsonObject.getString(MyManage.column_Source);

                            myManage.addValue(2,strFood,strPrice,strSource);

                            break;
                    }
                }//for

            } catch (Exception e){
                Log.d("Reset", "Update SQLite ==>" +e.toString());
            }
            
            



            intTable +=1;
        }

    } //sysJSON

    private void deleteAllSQLite() {
        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.database_name,
                MODE_PRIVATE, null);
        sqLiteDatabase.delete(MyManage.user_table, null, null);
        sqLiteDatabase.delete(MyManage.food_table, null, null);
    }

    private void testAddValue() {
        myManage.addValue(1, "user", "pass", "name");
        myManage.addValue(2, "food", "price", "source");
    }
}// Main Class
