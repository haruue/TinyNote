package cn.com.caoyue.tinynote;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import com.jude.utils.JUtils;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MessageDB extends SQLiteOpenHelper {

    SqlBrite sqlBrite;
    BriteDatabase db;
    final static String DATABASE_NAME = "MessageDB";
    Handler handler;

    public interface DatebaseListener {

        void onDatabaseChnage();
        void onQueryResult(ArrayList<MessageItem> itemArray);

    }

    public static final String CREATE_MESSAGEDATABASE = "create table " + DATABASE_NAME + " ("
            + "id integer primary key autoincrement, "
            + "time TimeStamp NOT NULL DEFAULT (datetime('now','localtime')), "
            + "sign integer default 0, "
            + "message text, "
            + "hash text)";

    private Context context;
    private DatebaseListener listener;

    public MessageDB(Context context) {
        super(context, "message.db", null, 3);
        this.context = context;
        sqlBrite = SqlBrite.create();
        db = sqlBrite.wrapDatabaseHelper(this, Schedulers.io());
        handler = new Handler(context.getMainLooper());
    }

    public void setListener(DatebaseListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Looper.prepare();
        final ProgressDialog progressDialog = new ProgressDialog(context);
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        });
        final SQLiteDatabase database = db;
        database.execSQL(CREATE_MESSAGEDATABASE);
        ContentValues values = new ContentValues();
        values.put("sign", 0);
        values.put("message", context.getResources().getString(R.string.welcome));
        values.put("hash", JUtils.MD5(context.getResources().getString(R.string.welcome).getBytes()));
        database.insert(DATABASE_NAME, null, values);
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.hide();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Looper.prepare();
        final SQLiteDatabase database = db;
        switch (oldVersion) {
            case 2:
                final ProgressDialog progressDialog = new ProgressDialog(context);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.show();
                    }
                });
                database.execSQL("ALTER TABLE `" + DATABASE_NAME + "` ADD COLUMN hash text");
                Cursor cursor = database.rawQuery("SELECT * FROM `" + DATABASE_NAME + "`", new String[]{});
                if (cursor.moveToFirst()) {
                    do {
                        database.execSQL("UPDATE `" + DATABASE_NAME + "` SET `hash` = '" + JUtils.MD5(cursor.getString(cursor.getColumnIndex("message")).getBytes()) + "' WHERE `id` = " + cursor.getInt(cursor.getColumnIndex("id")));
                    } while (cursor.moveToNext());
                }
                cursor.close();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.hide();
                        progressDialog.dismiss();
                    }
                });
                break;
        }

    }

    public void getMessage() {
        final Observable<SqlBrite.Query> messages = db.createQuery(DATABASE_NAME, "SELECT * FROM `MessageDB`");
        messages.map(new Func1<SqlBrite.Query, ArrayList<MessageItem>>() {
            @Override
            public ArrayList<MessageItem> call(SqlBrite.Query query) {
                ArrayList<MessageItem> items = new ArrayList<MessageItem>(0);
                Cursor cursor = query.run();
                if (cursor == null) {
                    return items;
                }
                if (cursor.moveToFirst()) {
                    do {
                        items.add(new MessageItem(
                                cursor.getInt(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("time")),
                                cursor.getInt(cursor.getColumnIndex("sign")),
                                cursor.getString(cursor.getColumnIndex("message")),
                                cursor.getString(cursor.getColumnIndex("hash"))
                        ));
                    } while (cursor.moveToNext());
                }
                return items;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<MessageItem>>() {
                    @Override
                    public void call(ArrayList<MessageItem> messageItems) {
                        listener.onQueryResult(messageItems);
                    }
                });
    }

    public void insert(final String message, final int sign) {
        Observable.create(new Observable.OnSubscribe<MessageItem>() {
            @Override
            public void call(Subscriber<? super MessageItem> subscriber) {
                ContentValues values = new ContentValues();
                values.put("sign", sign);
                values.put("message", message);
                values.put("hash", JUtils.MD5(message.getBytes()));
                db.insert(DATABASE_NAME, values);
                values.clear();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MessageItem>() {
                    @Override
                    public void call(MessageItem item) {
                        listener.onDatabaseChnage();
                    }
                });
    }

    public void delete(final MessageItem item) {
        Observable.create(new Observable.OnSubscribe<MessageItem>() {
            @Override
            public void call(Subscriber<? super MessageItem> subscriber) {
                db.delete(DATABASE_NAME, "id=?", item.getId() + "");
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MessageItem>() {
                    @Override
                    public void call(MessageItem item) {
                        listener.onDatabaseChnage();
                    }
                });
    }

}
