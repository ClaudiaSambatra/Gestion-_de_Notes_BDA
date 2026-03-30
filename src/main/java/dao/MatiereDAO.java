package dao;

import model.Matiere;
import util.AppLog;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatiereDAO {
    public static List<Matiere> getAll() {
        List<Matiere> l = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); ResultSet r = c.createStatement().executeQuery("SELECT * FROM matiere ORDER BY num_mat")) {
            while (r.next()) l.add(new Matiere(r.getInt("num_mat"), r.getString("design"), r.getFloat("coef")));
        } catch (Exception e) {
            AppLog.error("MatiereDAO.getAll", e);
        }
        return l;
    }

    public static boolean insert(Matiere m) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("INSERT INTO matiere(num_mat,design,coef) VALUES(?,?,?)")) {
            p.setInt(1, m.getNumMat());
            p.setString(2, m.getDesign().trim());
            p.setFloat(3, m.getCoef());
            return p.executeUpdate() > 0;
        } catch (Exception e) {
            AppLog.error("MatiereDAO.insert", e);
            return false;
        }
    }

    public static boolean update(Matiere m) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE matiere SET design=?,coef=? WHERE num_mat=?")) {
            p.setString(1, m.getDesign().trim());
            p.setFloat(2, m.getCoef());
            p.setInt(3, m.getNumMat());
            return p.executeUpdate() > 0;
        } catch (Exception e) {
            AppLog.error("MatiereDAO.update", e);
            return false;
        }
    }

    public static boolean delete(int n) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM matiere WHERE num_mat=?")) {
            p.setInt(1, n);
            return p.executeUpdate() > 0;
        } catch (Exception e) {
            AppLog.error("MatiereDAO.delete", e);
            return false;
        }
    }
}
