package dao;

import model.AuditNote;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    public static List<AuditNote> getAllAudit() {
        List<AuditNote> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_note ORDER BY date_operation DESC";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Float noteAncien = rs.getObject("note_ancien") != null ? rs.getFloat("note_ancien") : null;
                Float noteNouv   = rs.getObject("note_nouv")  != null ? rs.getFloat("note_nouv")  : null;

                AuditNote a = new AuditNote(
                        rs.getString("type_operation"),
                        rs.getTimestamp("date_operation").toLocalDateTime(),
                        rs.getInt("num_etudiant"),
                        rs.getString("nom"),
                        rs.getString("design"),
                        noteAncien,
                        noteNouv,
                        rs.getString("utilisateur")
                );
                a.setId(rs.getInt("id"));
                list.add(a);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static long countByType(List<AuditNote> list, String type) {
        return list.stream().filter(a -> type.equals(a.getTypeOperation())).count();
    }
}
