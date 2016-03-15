package cn.com.caoyue.tinynote;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jude.utils.JUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<MessageItem> messageArray;
    private RecyclerView messageView;
    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(savedInstanceState);
        //控件
        ((Button) findViewById(R.id.send_button)).setOnClickListener(new ListenerInMain());
    }

    private void init(Bundle savedInstanceState) {
        JUtils.initialize(getApplication());
        JUtils.setDebug(BuildConfig.DEBUG, "TINYNOTE");
        //取得数据
        getMessage();
        //设定RecyclerView
        messageAdapter = new MessageAdapter(messageArray);
        messageView = (RecyclerView) findViewById(R.id.recyclerview_message_show);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        messageView.setLayoutManager(linearLayoutManager);
        messageView.setAdapter(messageAdapter);
        messageView.setItemAnimator(new DefaultItemAnimator());
        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                //直接在这里注册进行监听，添加数据则跳到底部
                messageView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
        messageAdapter.setOnItemClickListener(new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.animate().translationZ(15F).setDuration(300).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.animate().translationZ(1f).setDuration(500).start();
                        }
                    }).start();
                }
            }
        });
    }

    /**
     * 刷新 RecyclerView
     */
    private void refreshMessageView() {
        getMessage();
        //对Adapter更新数据，而不是重建Adapter
        messageAdapter.setData(messageArray);
    }

    /**
     * 创建/取得数据表中的数据
     * 数据库操作写在在Activity中且在主线程进行不太优雅。
     * 可以尝试使用Rxjava+sqlbrite，从数据库订阅数据变动通知，就不用每次手动修改UI，会更优雅。
     */
    private void getMessage() {
        messageArray = new ArrayList<MessageItem>();
        //创建并取得数据表
        MessageDB messageDB = new MessageDB(this, "message.db", null, 2);
        SQLiteDatabase db = messageDB.getWritableDatabase();
        //从数据库中提取以前的数据
        Cursor cursor = db.query("MessageDB", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                messageArray.add(new MessageItem(
                        cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("time")),
                        cursor.getInt(cursor.getColumnIndex("sign")),
                        cursor.getString(cursor.getColumnIndex("message"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private class ListenerInMain implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.send_button:
                    //取得文本信息
                    EditText messageInput = (EditText) findViewById(R.id.edittext_message_input);
                    String newMessage = messageInput.getText().toString();
                    ContentValues values = new ContentValues();
                    values.put("message", newMessage);
                    //获取数据表
                    MessageDB messageDB = new MessageDB(MainActivity.this, "message.db", null, 2);
                    SQLiteDatabase db = messageDB.getWritableDatabase();
                    db.insert("MessageDB", null, values);
                    //刷新
                    refreshMessageView();
                    //清空输入域
                    messageInput.setText(R.string.blank);
                    break;
            }
        }
    }
}
