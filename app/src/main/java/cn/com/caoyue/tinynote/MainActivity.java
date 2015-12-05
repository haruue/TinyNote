package cn.com.caoyue.tinynote;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
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
    }
}
