package com.android.king.xmppdemo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.listener.OnSqliteUpdateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库工具类，支持多数据库并发
 *
 * @author：King
 * @time: 2018/9/8 13:26
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "xmpp";  //默认的数据库
    private static final int DB_VERSION = 1;
    private OnSqliteUpdateListener onSqliteUpdateListener;
    private static Map<String, SQLiteHelper> dbMaps = new HashMap<>();

    private static List<String> defaultTableList;
    /**
     * 建表语句列表
     */
    private List<String> createTableList;
    private String nowDbName;

    private SQLiteHelper(Context context, String dbName, int dbVersion, List<String> tableSqls) {
        super(context, dbName, null, dbVersion);
        nowDbName = dbName;
        createTableList = new ArrayList<>();
        createTableList.addAll(tableSqls);
    }

    public static void init(Context context, List<String> tableList) {
        defaultTableList = new ArrayList<>();
        defaultTableList.addAll(tableList);
        getInstance(context);
    }

    /**
     * 获取数据库实例
     *
     * @param context
     * @param dbName    数据库名称
     * @param dbVersion 数据库版本号
     * @param tableSqls 建表语句
     */
    public static SQLiteHelper getInstance(Context context, String dbName, int dbVersion, List<String> tableSqls) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(dbName);
        if (dataBaseOpenHelper == null) {
            dataBaseOpenHelper = new SQLiteHelper(context, dbName, dbVersion, tableSqls);
        }
        dbMaps.put(dbName, dataBaseOpenHelper);//HashMap:如果存在同名key则会覆盖value
        return dataBaseOpenHelper;
    }

    public static SQLiteHelper getInstance(Context context, String dbName, List<String> tableSqls) {
        return getInstance(context, dbName, DB_VERSION, tableSqls);
    }

    public static SQLiteHelper getMsgInstance(Context context, String dbName) {
        List<String> tableSqls = new ArrayList<>();
        tableSqls.add(AppConstants.CREATE_TABLE_MESSAGE);
        return getInstance(context, dbName, DB_VERSION, tableSqls);
    }

    public static SQLiteHelper getInstance(Context context) {
        return getInstance(context, DB_NAME, DB_VERSION, defaultTableList);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String sqlString : createTableList) {
            db.execSQL(sqlString);
        }
    }

    /**
     * 删除指定数据库
     *
     * @param context
     * @param dbName
     */
    public void deleteDatabase(Context context, String dbName) {
        try {
            SQLiteHelper dataBaseOpenHelper = dbMaps.get(dbName);
            if (dataBaseOpenHelper != null) {
                dataBaseOpenHelper.close();
                dbMaps.remove(dataBaseOpenHelper);
                dataBaseOpenHelper = null;
            }
            context.deleteDatabase(dbName);
        } catch (Exception e) {
        }
    }

    /**
     * @param @param sql
     * @param @param bindArgs
     * @return void
     * @Title: execSQL
     * @Description: Sql写入
     * @author lihy
     */
    public void execSQL(String sql, Object[] bindArgs) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getWritableDatabase();
            database.execSQL(sql, bindArgs);
        }
    }

    /**
     * @param @param  sql查询
     * @param @param  bindArgs
     * @param @return
     * @return Cursor
     * @Title: rawQuery
     * @Description:
     * @author lihy
     */
    public Cursor rawQuery(String sql, String[] bindArgs) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(sql, bindArgs);
            return cursor;
        }
    }

    /**
     * @param @param table
     * @param @param contentValues 设定文件
     * @return void 返回类型
     * @throws
     * @Title: insert
     * @Description: 插入数据
     * @author lihy
     */
    public void insert(String table, ContentValues contentValues) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getWritableDatabase();
            database.insert(table, null, contentValues);
        }
    }

    /**
     * @param @param table
     * @param @param values
     * @param @param whereClause
     * @param @param whereArgs 设定文件
     * @return void 返回类型
     * @throws
     * @Title: update
     * @Description: 更新
     */
    public void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getWritableDatabase();
            database.update(table, values, whereClause, whereArgs);
        }
    }

    /**
     * @param @param table
     * @param @param whereClause
     * @param @param whereArgs
     * @return void
     * @Title: delete
     * @Description:删除
     * @author lihy
     */
    public void delete(String table, String whereClause, String[] whereArgs) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getWritableDatabase();
            database.delete(table, whereClause, whereArgs);
        }
    }

    /**
     * @param @param table
     * @param @param columns
     * @param @param selection
     * @param @param selectionArgs
     * @param @param groupBy
     * @param @param having
     * @param @param orderBy
     * @return void
     * @Title: query
     * @Description: 查
     * @author lihy
     */
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getReadableDatabase();
            // Cursor cursor = database.rawQuery("select * from "
            // + TableName.TABLE_NAME_USER + " where userId =" + userId, null);
            Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            return cursor;
        }
    }

    /**
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return Cursor
     * @Description:查
     * @exception:
     * @author: lihy
     * @time:2015-4-3 上午9:37:29
     */
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getReadableDatabase();
            // Cursor cursor = database.rawQuery("select * from "
            // + TableName.TABLE_NAME_USER + " where userId =" + userId, null);
            Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            return cursor;
        }
    }

    /**
     * @param @return
     * @return Cursor
     * @Description 查询，方法重载,table表名，sqlString条件
     * @author lihy
     */
    public Cursor query(String tableName, String sqlString) {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        synchronized (dataBaseOpenHelper) {
            SQLiteDatabase database = dataBaseOpenHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from " + tableName + " " + sqlString, null);

            return cursor;
        }
    }

    /**
     * 关闭
     */
    public void clear() {
        SQLiteHelper dataBaseOpenHelper = dbMaps.get(nowDbName);
        if (dataBaseOpenHelper != null) {
            dataBaseOpenHelper.close();
            dbMaps.remove(dataBaseOpenHelper);
            dataBaseOpenHelper = null;
        }
    }

    /**
     * onUpgrade()方法在数据库版本每次发生变化时都会把用户手机上的数据库表删除，然后再重新创建。<br/>
     * 一般在实际项目中是不能这样做的，正确的做法是在更新数据库表结构时，还要考虑用户存放于数据库中的数据不会丢失,从版本几更新到版本几。(非
     * Javadoc)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        if (onSqliteUpdateListener != null) {
            onSqliteUpdateListener.onSqliteUpdateListener(db, arg1, arg2);
        }
    }

    public void setOnSqliteUpdateListener(OnSqliteUpdateListener onSqliteUpdateListener) {
        this.onSqliteUpdateListener = onSqliteUpdateListener;
    }
}
