package com.kcube.cst.ktnt.trns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.kcube.ekp.bbs.BdItem;
import com.kcube.lib.jdbc.ClobSupport;
import com.kcube.lib.jdbc.DbService;
import com.kcube.lib.jdbc.DbStorage;
import com.kcube.lib.sql.SqlDate;
import com.kcube.lib.sql.SqlInsert;
import com.kcube.lib.sql.SqlSelect;
import com.kcube.lib.sql.SqlUpdate;

public class BdItemTmpTrns {
	private static com.kcube.cst.ktnt.trns.BdItemTrnsConfig _bditemTrnsConfig =
		(com.kcube.cst.ktnt.trns.BdItemTrnsConfig )
			com.kcube.sys.conf.ConfigService.getConfig(
				com.kcube.cst.ktnt.trns.BdItemTrnsConfig.class);
	static DbStorage _storage = new DbStorage(BdItem.class);
	
	/**
	 * main 에서는 content 를 제외한 날리지큐브 테이블에 필요한 데이터들을 가져오는 작업을 한다.
	 * */
	public static void main(String[] args) //content, userid 를 제외한 값들을 임시테이블로 가져온다
	{
		System.out.println("Start !!!");
		Connection myConn = null; //informix 접속
    	Statement stmt = null;
    	ResultSet rs = null;
    	String testSql = "";
    	
    	/**
    	 * BdItemTrnsConfig.properties 파일에 인포믹스로부터 가져올 해당 맵 ID 와 
    	 * 이관되서 저장될 맵을 지정해 주기 위해 날리지큐브 테이블의 level 2, 3, 4 를 나열했다. (level 1 은 동일하므로 아래 쿼리에 직접 넣었음)
    	 * ex) 인포믹스 db 에 맵 아이디가 'A0001-021-002-015' 인 맵의 모든 게시물을 가져오는데
    	 *     level1 -> 4000 , level2 -> 10155 , level3 -> 10181 , level4 -> 10190 라면 
    	 *     
    	 *     mapId={'A0001-021-002-015'}
    	 *     level2={10155}
    	 *     level3={10181}
    	 *     level4={10190}
    	 *     
    	 *     라고 BdItemTrnsConfig.properties 에 입력해주면 된다.
    	 *     두개 이상의 맵에서 한번에 가져오고 싶다면 위의 배열에서 콤마로 구분해서 추가로 넣어주면 된다.
    	 *     ex) mapId={'A0001-021-002-015', 'A0001-021-002-016'}
    	 *         level2={10155, 10156}
    	 *         level3={10181, 10199}
    	 *         level4={10190, 10213}
    	 *   
    	 * */
    	for(int mapid = 0 ; mapid < _bditemTrnsConfig.getMapId().length ; mapid++){
		testSql = "select l.title, l.makeusername, l.makedatetime, l.map_id, l.makeuserid, l.id from t_bbs_content c, t_bbs_list l where l.id == c.id and l.map_id = '" + _bditemTrnsConfig.getMapId()[mapid] + "' order by l.makedatetime asc";

		    	try {
		    		myConn = BdItemTrnsConnection.getConnection();
		    	}catch ( Exception e ) {
		    		System.out.println("Statement Exception : " + e.toString());
		    	}
		    	
		    	try {
		    		stmt = myConn.createStatement();
		    	}catch ( Exception e ) {
					System.out.println("Statement Exception : " + e.toString());
				}
		
				try {
					
					rs = stmt.executeQuery(testSql);
					
				}catch ( Exception e) {
					System.out.println("Query Exception : " + e.toString());
				}
		
				//stmt1.executeQuery(sql2);
				
				try{
					
					while (rs.next()){
		
						try {
							String textDate = rs.getString(3);
							DateFormat fm = new SimpleDateFormat("yyyyMMddHHmmss");
							Date date = fm.parse(textDate);
							SqlInsert ins = new SqlInsert("BD_ITEM");
							ins.setSequence("itemid", "SQ_BD_ITEM");
							ins.setSequence("gid", "SQ_BD_ITEM");
							ins.setLong("level1", 4000); //업무게시판
							ins.setLong("level2", _bditemTrnsConfig.getLevel2()[mapid]);
							ins.setLong("level3", _bditemTrnsConfig.getLevel3()[mapid]);
							ins.setLong("level4", _bditemTrnsConfig.getLevel4()[mapid]);
							ins.setLong("status", 3000);
							ins.setLong("isvisb", 1);
							ins.setLong("kmid", (_bditemTrnsConfig.getLevel4()[mapid] != 0 ? _bditemTrnsConfig.getLevel4()[mapid] : (_bditemTrnsConfig.getLevel3()[mapid] != 0 ? _bditemTrnsConfig.getLevel3()[mapid] : _bditemTrnsConfig.getLevel2()[mapid])));
							ins.setLong("pos", 0);
							ins.setString("TITLE",rs.getString(1));
							ins.setString("RGST_NAME",rs.getString(2));
							ins.setString("AUTH_NAME",rs.getString(2));
							ins.setLong("AUTH_USERID",10000);
							ins.setLong("RGST_USERID",10000);
							ins.setTimestamp("RGST_DATE",SqlDate.getTimestamp(date));
							ins.setTimestamp("LAST_UPDT",SqlDate.getTimestamp(date));
							ins.setString("trns_src",rs.getString(4));
							ins.setString("cstm_field1", rs.getString(5));
							ins.setString("trns_key",rs.getString(6));
							
							ins.execute();
							DbService.commit();
							
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					stmt.close();
					if (rs != null) {            	
						rs.close();            	
					}
					
				}
				catch(Exception e){
					System.out.println("Data view Exception " + e.toString()); 
				}
				BdItemTrnsConnection.close(myConn);
    	}
		
	}
	
	/**
	 * main2 에서는 위 main 에서 가져오지 않은 content 를 가져와서 update 한다.
	 * clob 가 너무 큰 경우 톰캣 화면에 시스템아웃 문구들이 한동안 안나와 멈춘건지 진행중인지 알 수 없으므로
	 * 현재 가져온 clob의 맵 아이디와 시스템의 메모리를 찍는 구문을 추가했다.
	 * 이 메소드에도 BdItemTrnsConfig.properties 읽는 구문이 들어있다.
	 * */
	public static void main2(String[] args)//content를 임시테이블로 가져온다(clob 처리)
	{
		Connection myConn = null; //informix 접속
 		Statement stmt = null;
 		Statement cntStmt = null;
 		PreparedStatement cntPstmt = null;
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		ResultSet rs2 = null;
 		ResultSet id = null;
 		ResultSet rsCnt = null;
 		String testSql = "";
 		String cnt = "";
 		cnt = "select count(*) from t_bbs_content c, t_bbs_list l where l.id == c.id and l.map_id = ?";
 		testSql = "select c.content, l.id from t_bbs_content c, t_bbs_list l where l.id == c.id and l.map_id = ? and l.id = ?";
 		int count = 0;
 		int i = 0;
 		Runtime runtime = Runtime.getRuntime();
 		
 		try
		{
			myConn = BdItemTrnsConnection.getConnection();
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
 		for(int mapid = 0 ; mapid < _bditemTrnsConfig.getMapId().length ; mapid++){
 			System.out.println("mapid = " + _bditemTrnsConfig.getMapId()[mapid]);
 			try
			{
 				cntPstmt = myConn.prepareStatement(cnt);
 				cntPstmt.setString(1, _bditemTrnsConfig.getMapId()[mapid]);
				rsCnt = cntPstmt.executeQuery();
				while(rsCnt.next()){
					count = rsCnt.getInt(1);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				rsCnt.close();
				cntPstmt.close();
//				myConn.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			String[] tmp;
	 		tmp = new String[count];
	 		SqlSelect sel = new SqlSelect();
			sel.select("trns_key");
 			sel.from("bd_item");
 			sel.where("trns_src = ?", _bditemTrnsConfig.getMapId()[mapid]);
 			sel.order("rgst_date asc");
 			try
			{
				id = sel.query();
				while(id.next()){
	 				tmp[i] = id.getString(1).toString();
	 				i = i+1;
	 				
	 			}
				id.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
 			
 			i = 0;
			int rcnt = 1;
			while(rcnt <= count){
				
//				testSql = "select c.content, l.id from t_bbs_content c, t_bbs_list l where l.id == c.id and l.map_id = '" + _bditemTrnsConfig.getMapId()[mapid] + "' and l.id = '" + tmp[rcnt-1] + "'";
				
		 		/*try {
		 			myConn = BdItemTrnsConnection.getConnection();
		 			
		 		}catch ( Exception e ) {
		 			System.out.println("Statement Exception : " + e.toString());
		 		}*/
		 	
//		 		try {
//		 			stmt = myConn.createStatement();
//		 		}catch ( Exception e ) {
//					System.out.println("Statement Exception : " + e.toString());
//				}

				try {
					
					pstmt = myConn.prepareStatement(testSql);
		 			pstmt.setString(1, _bditemTrnsConfig.getMapId()[mapid]);
		 			pstmt.setString(2, tmp[rcnt-1]);
					
					rs = pstmt.executeQuery(); //인포믹스 content
					
				}catch ( Exception e) {
					System.out.println("Query Exception : " + e.toString());
				}
				try{
						while(rs.next()){
							try {
								String content = "";
								String trns = "";
								SqlSelect sel2 = new SqlSelect(); //오라클 임시 테이블
								sel2.select("itemid, content");
								sel2.from("bd_item");
								sel2.where("trns_key = ?", rs.getString(2));
								rs2 = sel2.query();
								/*
								FileReader fr = new FileReader("http://infoshop.ktn.co.kr/xware_kts_resource/lib/app/AppPsr.js");
								File f = new File("http://infoshop.ktn.co.kr/xware_kts_resource/lib/app/AppPsr.js");
								BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
								*/
								while(rs2.next()){
									content = ClobSupport.read(rs.getClob(1)) == null ? "" : ClobSupport
										.read(rs.getClob(1));
									/*content = content.replaceAll("&lt;", "<");
									content = content.replaceAll("&gt;", ">");
									content = content.replaceAll("&quot;", "\"");
									content = content.replaceAll("&amp;", "&");
									content = content.replaceAll("&apos;", "\'");*/
									BdItem item = (BdItem) _storage.load(rs2.getLong(1));
									/*trns = content.replaceAll("<script language=\"javaScript\" SRC = \"http:\\/\\/infoshop.ktn.co.kr\\/xware_kts_resource\\/lib\\/app\\/AppPsr.js\"><\\/script>" , "");
									trns = trns.replaceAll("<script language=\"javaScript\" SRC = \"http:\\/\\/infoshop.ktn.co.kr\\/xware_kts_resource\\/lib\\/common\\/LibString.js\"><\\/script>", "");
									trns = trns.replaceAll("<script language=\"javaScript\" SRC = \"http:\\/\\/infoshop.ktn.co.kr\\/xware_kts_resource\\/lib\\/app\\/AppAddDraft.js\"><\\/script>", "");
									trns = trns.replaceAll("document.all.namedItem\\(\'psrViewFrame\'\\).style.visibility=\"visible\";", "");
									trns = trns.replaceAll("\\/xware_kts_resource\\/images\\/pub\\/checkbox.gif", "http:\\/\\/infoshop.ktn.co.kr\\/xware_kts_resource\\/images\\/pub\\/checkbox.gif");
									trns = trns.replaceAll("_psr_file_    = \"\";", "alert\\(\'111\'\\); _psr_file_    = \"\";");
									*/
									
									item.setContent(content);
									DbService.commit();
									System.out.println("rcnt = " + rcnt);
									System.out.println("mapid = " + _bditemTrnsConfig.getMapId()[mapid]);
									System.out.println("used memory : " + (runtime.totalMemory() - runtime.freeMemory()) + " byte");
									System.out.println("free memory : " + runtime.freeMemory() + " byte");
								}
								if (rs2 != null)
									rs2.close();
								
							} catch (Exception e)
							{
								e.printStackTrace();
							}
								
						}
					
					if (rs != null) {            	
						rs.close();            	
					}
					pstmt.close();
				}catch(Exception e){
					System.out.println("Data view Exception " + e.toString()); 
				}
			
				rcnt = rcnt + 1;
			}
			
 		}
 		BdItemTrnsConnection.close(myConn);
	}
	
	/**
	 * main3 에서는 게시물의 작성자를 넣어주는 작업을 한다.( 인포믹스에서 main 메소드를 통해 직접 가져왔으면 좋겠지만 
	 * 그게 안되서 main3 메소드를 만들어 추가로 진행하게끔 만듬.
	 * 여기에도 BdItemTrnsConfig.properties 읽는 구문이 있다.
	 * */
	public static void main3(String[] args)//userid를 임시테이블에 추가한다(hr_user 와 조인)
	{
		
		ResultSet rs = null;
		ResultSet rs2 = null;
		for(int mapid = 0 ; mapid < _bditemTrnsConfig.getMapId().length ; mapid++){
		SqlSelect sel = new SqlSelect();
		sel.select("trns_key, cstm_field1");
		sel.from("bd_item");
		sel.where("trns_src = ?", _bditemTrnsConfig.getMapId()[mapid]);
		sel.order("rgst_date asc");
		
		
		try
		{
			rs = sel.query();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			while(rs.next()){
				SqlSelect sel2 = new SqlSelect();
				sel2.select("h.userid, h.user_disp, b.rgst_name, b.trns_key");
				sel2.from("bd_item b, hr_user h");
				sel2.where("h.sabun = ?", rs.getString(2));
				sel2.where("b.trns_key = ?", rs.getString(1));
				sel2.order("b.rgst_date asc");
				
				try
				{
					rs2 = sel2.query();
					while(rs2.next()){
						SqlUpdate upd = new SqlUpdate("BD_ITEM");
						upd.setLong("auth_userid", rs2.getLong(1));
						upd.setLong("rgst_userid", rs2.getLong(1));
						upd.setString("auth_disp", (rs2.getString(2) == null ? rs2.getString(3) : rs2.getString(2)));
						upd.where("trns_key = ?", rs.getString(1));
						
						upd.execute();
						DbService.commit();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				if (rs2 != null)
					rs2.close();
			}
			if (rs != null) {            	
				rs.close();            	
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	}
}
