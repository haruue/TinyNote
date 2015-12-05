package cn.com.caoyue.tinynote;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> timeArray = new ArrayList<String>(0);
    HashMap<String, String> messageMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar 相关
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_inMain);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        //创建并取得数据表
        MessageDB messageDB = new MessageDB(this, "message.db", null, 2);
        SQLiteDatabase db = messageDB.getWritableDatabase();
        //从数据库中提取以前的数据
        Cursor cursor = db.query("MessageDB", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                timeArray.add(cursor.getString(cursor.getColumnIndex("time")));
                messageMap.put(cursor.getString(cursor.getColumnIndex("time")), cursor.getString(cursor.getColumnIndex("message")));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
