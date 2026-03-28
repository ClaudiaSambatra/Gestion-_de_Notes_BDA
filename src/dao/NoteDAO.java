package dao;

import model.Note;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    public static List<Note> getAll() {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT * FROM note ORDER BY num_etudiant, num_mat";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Note(
                        rs.getInt("num_etudiant"),
                        rs.getInt("num_mat"),
                        rs.getFloat("note")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static boolean insertNote(Note note) {
        String sql = "INSERT INTO note(num_etudiant, num_mat, note) VALUES(?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, note.getNumEtudiant());
            pst.setInt(2, note.getNumMat());
            pst.setFloat(3, note.getNote());
            pst.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean updateNote(Note note) {
        String sql = "UPDATE note SET note=? WHERE num_etudiant=? AND num_mat=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setFloat(1, note.getNote());
            pst.setInt(2, note.getNumEtudiant());
            pst.setInt(3, note.getNumMat());
            int rows = pst.executeUpdate();
            return rows > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteNote(Note note) {
        String sql = "DELETE FROM note WHERE num_etudiant=? AND num_mat=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, note.getNumEtudiant());
            pst.setInt(2, note.getNumMat());
            int rows = pst.executeUpdate();
            return rows > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
