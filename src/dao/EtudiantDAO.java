package dao;

import model.Etudiant;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtudiantDAO {

    public static List<Etudiant> getAll() {
        List<Etudiant> list = new ArrayList<>();
        String sql = "SELECT * FROM etudiant ORDER BY num_etudiant";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Etudiant(
                        rs.getInt("num_etudiant"),
                        rs.getString("nom"),
                        rs.getFloat("moyenne")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static boolean insert(Etudiant e) {
        String sql = "INSERT INTO etudiant(num_etudiant, nom, moyenne) VALUES(?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, e.getNumEtudiant());
            pst.setString(2, e.getNom());
            pst.setFloat(3, 0);
            pst.executeUpdate();
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    public static boolean update(Etudiant e) {
        String sql = "UPDATE etudiant SET nom=? WHERE num_etudiant=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, e.getNom());
            pst.setInt(2, e.getNumEtudiant());
            pst.executeUpdate();
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    public static boolean delete(int numEtudiant) {
        String sql = "DELETE FROM etudiant WHERE num_etudiant=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, numEtudiant);
            pst.executeUpdate();
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    public static Etudiant findById(int numEtudiant) {
        String sql = "SELECT * FROM etudiant WHERE num_etudiant=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, numEtudiant);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Etudiant(rs.getInt("num_etudiant"), rs.getString("nom"), rs.getFloat("moyenne"));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }
}
