package com.jit.blebeacon;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper
{
	public SQLiteDatabase db;

	public Context context;
	public String database_name;
	public int database_version;

	public DatabaseHandler(Context ctx, String db_name, int db_version)
	{
		super(ctx, db_name, null, db_version);
		context = ctx;
		database_name = db_name;
		database_version = db_version;
		
		db = this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// TODO Auto-generated method stub
		//Toast.makeText(context, "On Create Called", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
		//Toast.makeText(context, "On Upgrade Called", Toast.LENGTH_SHORT).show();
	}

	public void executeQuery(String query) // would execute one query
	{
		db.execSQL(query);
	}

	public Cursor executeSelect(String table_name, String[] columns_to_return,
			String where_conditions, String groupby_clause,
			String having_clause, String orderby_clause)
	{
		Cursor result = db.query(table_name, columns_to_return,
				where_conditions, null, groupby_clause, having_clause,
				orderby_clause);
		
		return result;
	}

	public void closeDatabase()
	{
		db.close();
	}
}
