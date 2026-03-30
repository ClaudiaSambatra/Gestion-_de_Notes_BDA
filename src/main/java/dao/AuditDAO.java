package dao;

import model.AuditNote;
import util.AppLog;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {
    public static List<AuditNote> getAll() {
        List<AuditNote> l = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); ResultSet r = c.createStatement().executeQuery("SELECT * FROM audit_note ORDER BY date_operation DESC")) {
            while (r.next()) {
                Float a = r.getObject("note_ancien") != null ? r.getFloat("note_ancien") : null;
                Float n = r.getObject("note_nouv") != null ? r.getFloat("note_nouv") : null;
                AuditNote au = new AuditNote(r.getString("type_operation"), r.getTimestamp("date_operation").toLocalDateTime(), r.getInt("num_etudiant"), r.getString("nom"), r.getString("design"), a, n, r.getString("utilisateur"));
                au.setId(r.getInt("id"));
                l.add(au);
            }
        } catch (Exception e) {
            AppLog.error("AuditDAO.getAll", e);
        }
        return l;
    }
}
