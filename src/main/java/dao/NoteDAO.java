package dao;

import model.Note;
import model.NoteView;
import util.AppLog;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {
    public static List<NoteView> getAllView() {
        List<NoteView> l = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); ResultSet r = c.createStatement().executeQuery("SELECT n.num_etudiant,e.nom,n.num_mat,m.design,n.note,m.coef FROM note n JOIN etudiant e ON e.num_etudiant=n.num_etudiant JOIN matiere m ON m.num_mat=n.num_mat ORDER BY e.nom,m.design")) {
            while (r.next())
                l.add(new NoteView(r.getInt("num_etudiant"), r.getString("nom"), r.getInt("num_mat"), r.getString("design"), r.getFloat("note"), r.getFloat("coef")));
        } catch (Exception e) {
            AppLog.error("NoteDAO.getAllView", e);
        }
        return l;
    }

    /**
     * CUD operations accept username for audit trail via session variable
     */
    public static boolean insert(Note n, String username) {
        try (Connection c = DatabaseConnection.getConnection()) {
            DatabaseConnection.setSessionUser(c, username);
            try (PreparedStatement p = c.prepareStatement("INSERT INTO note(num_etudiant,num_mat,note) VALUES(?,?,?)")) {
                p.setInt(1, n.getNumEtudiant());
                p.setInt(2, n.getNumMat());
                p.setFloat(3, n.getNote());
                return p.executeUpdate() > 0;
            }
        } catch (Exception e) {
            AppLog.error("NoteDAO.insert", e);
            return false;
        }
    }

    public static boolean update(Note n, String username) {
        try (Connection c = DatabaseConnection.getConnection()) {
            DatabaseConnection.setSessionUser(c, username);
            try (PreparedStatement p = c.prepareStatement("UPDATE note SET note=? WHERE num_etudiant=? AND num_mat=?")) {
                p.setFloat(1, n.getNote());
                p.setInt(2, n.getNumEtudiant());
                p.setInt(3, n.getNumMat());
                return p.executeUpdate() > 0;
            }
        } catch (Exception e) {
            AppLog.error("NoteDAO.update", e);
            return false;
        }
    }

    public static boolean delete(int ne, int nm, String username) {
        try (Connection c = DatabaseConnection.getConnection()) {
            DatabaseConnection.setSessionUser(c, username);
            try (PreparedStatement p = c.prepareStatement("DELETE FROM note WHERE num_etudiant=? AND num_mat=?")) {
                p.setInt(1, ne);
                p.setInt(2, nm);
                return p.executeUpdate() > 0;
            }
        } catch (Exception e) {
            AppLog.error("NoteDAO.delete", e);
            return false;
        }
    }

    public static boolean exists(int ne, int nm) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT 1 FROM note WHERE num_etudiant=? AND num_mat=?")) {
            p.setInt(1, ne);
            p.setInt(2, nm);
            return p.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }
}
