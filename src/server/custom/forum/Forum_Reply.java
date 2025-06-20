package server.custom.forum;

import java.util.Collection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import tools.FileoutputUtil;
import database.DatabaseConnection;
import java.util.Iterator;
import java.util.ArrayList;

public class Forum_Reply
{
    private int replyId;
    private int threadId;
    private int characterId;
    private String characterName;
    private String releaseTime;
    private String news;
    private static ArrayList<Forum_Reply> allReply;
    
    public Forum_Reply() {
    }
    
    public Forum_Reply(final int replyId, final int threadId, final int characterId, final String characterName, final String releaseTime, final String news) {
        this.replyId = replyId;
        this.threadId = threadId;
        this.characterId = characterId;
        this.characterName = characterName;
        this.releaseTime = releaseTime;
        this.news = news;
    }
    
    public int getReplyId() {
        return this.replyId;
    }
    
    public void setReplyId(final int replyId) {
        this.replyId = replyId;
    }
    
    public int getThreadId() {
        return this.threadId;
    }
    
    public void setThreadId(final int threadId) {
        this.threadId = threadId;
    }
    
    public int getCharacterId() {
        return this.characterId;
    }
    
    public void setCharacterId(final int characterId) {
        this.characterId = characterId;
    }
    
    public String getCharacterName() {
        return this.characterName;
    }
    
    public void setCharacterName(final String characterName) {
        this.characterName = characterName;
    }
    
    public String getReleaseTime() {
        return this.releaseTime;
    }
    
    public void setReleaseTime(final String releaseTime) {
        this.releaseTime = releaseTime;
    }
    
    public String getNews() {
        return this.news;
    }
    
    public void setNews(final String news) {
        this.news = news;
    }
    
    public static ArrayList<Forum_Reply> getAllReply() {
        return Forum_Reply.allReply;
    }
    
    public static void setAllReply(final ArrayList<Forum_Reply> allReply) {
        Forum_Reply.allReply = allReply;
    }
    
    public static ArrayList<Forum_Reply> getCurrentAllReply(final int tid) {
        final ArrayList<Forum_Reply> CurrentReply = new ArrayList<Forum_Reply>();
        for (final Forum_Reply fr : Forum_Reply.allReply) {
            if (fr.getThreadId() == tid) {
                CurrentReply.add(fr);
            }
        }
        return CurrentReply;
    }
    
    public static ArrayList<Forum_Reply> loadAllReply() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM forum_reply");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Forum_Reply.allReply.add(new Forum_Reply(rs.getInt("rid"), rs.getInt("tid"), rs.getInt("cid"), rs.getString("cname"), rs.getString("time"), rs.getString("news")));
            }
            rs.close();
            ps.close();
            return Forum_Reply.allReply;
        }
        catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/鏁版嵁搴撳紓甯?txt", (Throwable)ex);
            return null;
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    public static boolean addReply(final int tid, final int cid, final String cname, final String news) {
        Connection con = DatabaseConnection.getConnection();
        try {
            final StringBuilder query = new StringBuilder();
            query.append("INSERT INTO forum_reply(tid, cid, cname, news) VALUES (?,?,?,?)");
            final PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, tid);
            ps.setInt(2, cid);
            ps.setString(3, cname);
            ps.setString(4, news);
            ps.executeUpdate();
            ps.close();
            Forum_Reply.allReply.add(getReplyByNameToSql(tid, news));
            Forum_Thread.updateThreadReply(tid);
            return true;
        }
        catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/鏁版嵁搴撳紓甯?txt", (Throwable)ex);
            return false;
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    public static Forum_Reply getReplyById(final int rid) {
        for (final Forum_Reply fr : Forum_Reply.allReply) {
            if (fr.getReplyId() == rid) {
                return fr;
            }
        }
        return null;
    }
    
    public static Forum_Reply getReplyByNameToSql(final int tid, final String news) {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM forum_reply WHERE tid = ? AND news = ?");
            ps.setInt(1, tid);
            ps.setString(2, news);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Forum_Reply(rs.getInt("rid"), rs.getInt("tid"), rs.getInt("cid"), rs.getString("cname"), rs.getString("time"), rs.getString("news"));
            }
        }
        catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/鏁版嵁搴撳紓甯?txt", (Throwable)ex);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
        return null;
    }
    
    public static boolean deleteReply(final int tid, final int rid, final boolean isAll) {
        Connection con = DatabaseConnection.getConnection();
        try {
            boolean isExist = false;
            if (isAll) {
                if (getCurrentAllReply(tid) != null) {
                    Forum_Reply.allReply.removeAll((Collection<?>)getCurrentAllReply(tid));
                    isExist = true;
                }
            }
            else if (getReplyById(rid) != null) {
                Forum_Reply.allReply.remove((Object)getReplyById(rid));
                isExist = true;
            }
            if (!isExist) {
                return isExist;
            }
            final StringBuilder query = new StringBuilder();
            if (isAll) {
                query.append("DELETE FROM forum_reply WHERE tid = ?");
            }
            else {
                query.append("DELETE FROM forum_reply WHERE tid = ? AND rid = ?");
            }
            final PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, tid);
            if (!isAll) {
                ps.setInt(2, rid);
            }
            ps.executeUpdate();
            ps.close();
            return true;
        }
        catch (SQLException ex) {
            FileoutputUtil.outputFileError("logs/鏁版嵁搴撳紓甯?txt", (Throwable)ex);
            return false;
        }
    }
    
    static {
        Forum_Reply.allReply = new ArrayList<Forum_Reply>();
    }
}
