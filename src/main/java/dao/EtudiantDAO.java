package dao;

import model.Etudiant;
import util.AppLog;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtudiantDAO {
    public static List<Etudiant> getAll() {
        List<Etudiant> l = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); ResultSet r = c.createStatement().executeQuery("SELECT * FROM etudiant ORDER BY num_etudiant")) {
            while (r.next()) l.add(new Etudiant(r.getInt("num_etudiant"), r.getString("nom"), r.getFloat("moyenne")));
        } catch (Exception e) {
            AppLog.error("EtudiantDAO.getAll", e);
        }
        return l;
    }

    public static boolean insert(Etudiant e) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("INSERT INTO etudiant(num_etudiant,nom,moyenne) VALUES(?,?,0)")) {
            p.setInt(1, e.getNumEtudiant());
            p.setString(2, e.getNom().trim());
            return p.executeUpdate() > 0;
        } catch (Exception x) {
            AppLog.error("EtudiantDAO.insert", x);
            return false;
        }
    }

    public static boolean update(Etudiant e) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE etudiant SET nom=? WHERE num_etudiant=?")) {
            p.setString(1, e.getNom().trim());
            p.setInt(2, e.getNumEtudiant());
            return p.executeUpdate() > 0;
        } catch (Exception x) {
            AppLog.error("EtudiantDAO.update", x);
            return false;
        }
    }

    public static boolean delete(int n) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM etudiant WHERE num_etudiant=?")) {
            p.setInt(1, n);
            return p.executeUpdate() > 0;
        } catch (Exception x) {
            AppLog.error("EtudiantDAO.delete", x);
            return false;
        }
    }

    public static boolean exists(int n) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT 1 FROM etudiant WHERE num_etudiant=?")) {
            p.setInt(1, n);
            return p.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }
}
