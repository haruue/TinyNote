package cn.com.caoyue.tinynote;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.jude.utils.JUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MessageDB.DatebaseListener {

    ArrayList<MessageItem> messageArray = new ArrayList<MessageItem>();
    private RecyclerView messageView;
    private MessageAdapter messageAdapter;
    private MessageItem selectedItem;
    MessageDB helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(savedInstanceState);
        //控件
        (findViewById(R.id.send_button)).setOnClickListener(new ListenerInMain());
    }

    private void init(Bundle savedInstanceState) {
        JUtils.initialize(getApplication());
        JUtils.setDebug(BuildConfig.DEBUG, "TINYNOTE");
        //初始化数据库
        helper = new MessageDB(this);
        helper.setListener(this);
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
                messageView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
        registerForContextMenu(messageView);
        messageAdapter.setOnItemClickListener(new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.animate().translationZ(15F).setDuration(300).setListener(new AnimatorListenerAdapter() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.animate().translationZ(1f).setDuration(500).start();
                        }
                    }).start();
                }
            }

            @Override
            public boolean onItemLongClick(View view, int position) {
                selectedItem = messageAdapter.getData(position);
                return false;
            }
        });
        //获取数据
        getMessage();
    }

    /**
     * 刷新 RecyclerView
     */
    private void refreshMessageView() {
        getMessage();
    }

    /**
     * 创建/取得数据表中的数据
     * 数据库操作写在在Activity中且在主线程进行不太优雅。
     * 可以尝试使用Rxjava+sqlbrite，从数据库订阅数据变动通知，就不用每次手动修改UI，会更优雅。
     */
    private void getMessage() {
        helper.getMessage();
    }

    @Override
    public void onDatabaseChnage() {
        refreshMessageView();
    }

    @Override
    public void onQueryResult(ArrayList<MessageItem> itemArray) {
        messageAdapter.setData(itemArray);
        messageAdapter.notifyDataSetChanged();
    }

    private class ListenerInMain implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.send_button:
                    //取得文本信息
                    EditText messageInput = (EditText) findViewById(R.id.edittext_message_input);
                    final String newMessage = messageInput.getText().toString();
                    final ContentValues values = new ContentValues();
                    helper.insert(newMessage, 0);
                    //清空输入域
                    messageInput.setText(R.string.blank);
                    break;
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        JUtils.Log("inContextMenu");
        if (selectedItem == null) {
            return;
        }
        menu.setHeaderTitle(selectedItem.getMessage());
        menu.add(0, 0, 0, R.string.copy_to_clipboard);
        menu.add(0, 1, 0, R.string.delete_this_message);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                JUtils.copyToClipboard(selectedItem.getMessage());
                JUtils.Toast(getResources().getString(R.string.copy_to_clipboard_success));
                break;
            case 1:
                deleteMessage(selectedItem);
                break;
        }
        return true;
    }

    private void deleteMessage(MessageItem item) {
        helper.delete(item);
    }
}
