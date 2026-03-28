package dao;

import model.Matiere;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatiereDAO {

    public static List<Matiere> getAll() {
        List<Matiere> list = new ArrayList<>();
        String sql = "SELECT * FROM matiere ORDER BY num_mat";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Matiere(
                        rs.getInt("num_mat"),
                        rs.getString("design"),
                        rs.getFloat("coef")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static boolean insert(Matiere m) {
        String sql = "INSERT INTO matiere(num_mat, design, coef) VALUES(?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, m.getNumMat());
            pst.setString(2, m.getDesign());
            pst.setFloat(3, m.getCoef());
            pst.executeUpdate();
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    public static boolean update(Matiere m) {
        String sql = "UPDATE matiere SET design=?, coef=? WHERE num_mat=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, m.getDesign());
            pst.setFloat(2, m.getCoef());
            pst.setInt(3, m.getNumMat());
            pst.executeUpdate();
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    public static boolean delete(int numMat) {
        String sql = "DELETE FROM matiere WHERE num_mat=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, numMat);
            pst.executeUpdate();
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }
}
