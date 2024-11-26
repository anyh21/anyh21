package com.kcube.cst.ktnt.trns;

import java.io.*;
import java.sql.*;

import com.kcube.ekp.bbs.BdItem;
import com.kcube.ekp.kms.KItem;
import com.kcube.lib.action.Action;
import com.kcube.lib.action.ActionContext;
import com.kcube.lib.jdbc.ClobSupport;
import com.kcube.lib.jdbc.DbService;
import com.kcube.lib.jdbc.DbStorage;
import com.kcube.lib.sql.SqlDate;
import com.kcube.lib.sql.SqlInsert;
import com.kcube.lib.sql.SqlSelect;
import com.kcube.lib.sql.SqlUpdate;
import com.kcube.sys.usr.UserService;
import com.informix.jdbc.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BdItemTrns {
	private static com.kcube.cst.ktnt.trns.BdItemTrnsConfig _bditemTrnsConfig =
		(com.kcube.cst.ktnt.trns.BdItemTrnsConfig )
			com.kcube.sys.conf.ConfigService.getConfig(
				com.kcube.cst.ktnt.trns.BdItemTrnsConfig.class);
	static DbStorage _storage = new DbStorage(BdItem.class);
	
	/**
	 * main 과 main2 메소드는 bd_item_tmp 에 저장된 게시물들을 bd_item 으로 넣는 작업을 한다.
	 * main 은 content 를 제외한 데이터들을 넣는 작업
	 * main2 는 contemt 를 넣는 작업
	 * 
	 * 두개로 나눈 이유는 content 를 넣을 때 clobSupporter 를 쓰기 위해선 두 작업을 동시에 하게할 수 없어서
	 * 둘로 나눔.
	 * 
	 * 이 클래스를 쓰지 않고 bd_item_tmp -> bd_item 으로 insert select 구문을 이용해도 들어감(아마도)
	 * 
	 * 시간이 없어 급하게 이관해야 한다면 이 클래스를 사용하지 않고, BdItemTmpTrns 의 테이블명을 Bd_item 으로 다 바꾸고
	 * 직접 다이렉트로 데이터를 넣게 끔 한다.여유 있을 땐 임시테이블(BdItemTmpTrns)을 거치게 하자
	 * 
	 * 이 클래스에도 BdItemTrnsConfig.properties 읽는 구문이 들어있다. 프로퍼티스 작성법은 BdItemTmpTrns.java 참조
	 * 
	 * 이관을 완료한 후 분명히 데이터는 있는데 목록에서 안뜨는 경우라면
	 * 해당게시물이 level4 보다 깊은 레벨에 있는 경우이다.
	 * 따라서 jsp 폴더에 있는 '이관 완료 후 kmid 쿼리.txt' 참조하여 해당 kmid 를 update 시키는 쿼리를 따로 실행해준다.
	 * */
	public static void main(String[] args)
	{
		ResultSet rs = null;
		for(int mapid = 0 ; mapid < _bditemTrnsConfig.getMapId().length ; mapid++){
		SqlSelect sel = new SqlSelect();
		sel.select("status, isvisb, title, rgst_name, auth_name, auth_disp, auth_userid, rgst_userid, rgst_date, last_updt, " +
				"kmid, pos, trns_src, trns_key, level1, level2, level3, level4");
		sel.from("bd_item_tmp");
		sel.where("trns_src = ?", _bditemTrnsConfig.getMapId()[mapid]);
		sel.order("rgst_date asc");
			
		try
		{
			rs = sel.query();
			while(rs.next()){
				SqlInsert ins = new SqlInsert("bd_item");
				ins.setSequence("itemid", "SQ_BD_ITEM");
				ins.setSequence("gid", "SQ_BD_ITEM");
				ins.setLong("status", rs.getLong(1));
				ins.setLong("isvisb", rs.getLong(2));
				ins.setString("TITLE",rs.getString(3));
				ins.setString("RGST_NAME",rs.getString(4));
				ins.setString("AUTH_NAME",rs.getString(5));
				ins.setString("AUTH_DISP",rs.getString(6));
				ins.setLong("AUTH_USERID",rs.getLong(7));
				ins.setLong("RGST_USERID",rs.getLong(8));
				ins.setTimestamp("rgst_date", rs.getTimestamp(9));
				ins.setTimestamp("last_updt", rs.getTimestamp(10));
				ins.setLong("kmid",rs.getLong(11));
				ins.setLong("pos",rs.getLong(12));
				ins.setString("trns_src",rs.getString(13));
				ins.setString("trns_key",rs.getString(14));
				ins.setLong("level1", rs.getLong(15));
				ins.setLong("level2", rs.getLong(16));
				ins.setLong("level3", rs.getLong(17));
				ins.setLong("level4", rs.getLong(18));
				ins.execute();
				DbService.commit();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (rs != null) {            	
			try
			{
				rs.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}            	
		}
		}
	}
	
	public static void main2(String[] args)
	{
		Runtime runtime = Runtime.getRuntime();
		ResultSet rs = null;
		ResultSet rs2 = null;
		for(int mapid = 0 ; mapid < _bditemTrnsConfig.getMapId().length ; mapid++){
		SqlSelect sel = new SqlSelect();
		sel.select("content, trns_key, trns_src");
		sel.from("bd_item_tmp");
		sel.where("trns_src = ?", _bditemTrnsConfig.getMapId()[mapid]);
		sel.order("rgst_date asc");
			
		try
		{
			rs = sel.query();
			while(rs.next()){
				SqlSelect sel2 = new SqlSelect();
				sel2.select("itemid, content");
				sel2.from("bd_item");
				sel2.where("trns_key = ?", rs.getString(2));
				rs2 = sel2.query();
				while(rs2.next()){
					SqlUpdate upd = new SqlUpdate("bd_item");
					upd.setString("content", rs.getString(1));
					upd.where("trns_key = ?", rs.getString(2));
					upd.where("trns_src = ?", rs.getString(3));
					upd.execute();
					DbService.commit();
					System.out.println("mapid = " + _bditemTrnsConfig.getMapId()[mapid]);
					System.out.println("used memory : " + (runtime.totalMemory() - runtime.freeMemory()) + " byte");
					System.out.println("free memory : " + runtime.freeMemory() + " byte");
				}
				if (rs2 != null)
					rs2.close();
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (rs != null) {            	
			try
			{
				rs.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}            	
		}
	}
	}
}
