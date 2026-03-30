package dao;

import model.Utilisateur;
import util.AppLog;
import util.DatabaseConnection;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    public static Utilisateur authenticate(String login, String password) {
        String hash = PasswordUtil.hash(password);
        String sql = "SELECT * FROM utilisateur WHERE login=? AND mot_de_passe=? AND actif=true";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, login.trim().toLowerCase());
            p.setString(2, hash);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) return map(r);
            }
        } catch (Exception e) {
            AppLog.error("UtilisateurDAO.auth", e);
        }
        return null;
    }

    public static List<Utilisateur> getAll() {
        List<Utilisateur> l = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             ResultSet r = c.createStatement().executeQuery("SELECT * FROM utilisateur ORDER BY role,nom")) {
            while (r.next()) l.add(map(r));
        } catch (Exception e) {
            AppLog.error("UtilisateurDAO.getAll", e);
        }
        return l;
    }

    public static boolean insert(Utilisateur u) {
        String sql = "INSERT INTO utilisateur(login,mot_de_passe,nom,prenom,role,actif) VALUES(?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, u.getLogin().trim().toLowerCase());
            p.setString(2, PasswordUtil.hash(u.getMotDePasse()));
            p.setString(3, u.getNom().trim());
            p.setString(4, u.getPrenom() != null ? u.getPrenom().trim() : "");
            p.setString(5, u.getRole());
            p.setBoolean(6, u.isActif());
            return p.executeUpdate() > 0;
        } catch (Exception e) {
            AppLog.error("UtilisateurDAO.insert", e);
            return false;
        }
    }

    public static boolean update(Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom=?,prenom=?,role=?,actif=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, u.getNom().trim());
            p.setString(2, u.getPrenom() != null ? u.getPrenom().trim() : "");
            p.setString(3, u.getRole());
            p.setBoolean(4, u.isActif());
            p.setInt(5, u.getId());
            return p.executeUpdate() > 0;
        } catch (Exception e) {
            AppLog.error("UtilisateurDAO.update", e);
            return false;
        }
    }

    public static boolean updatePassword(int id, String newPassword) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE utilisateur SET mot_de_passe=? WHERE id=?")) {
            p.setString(1, PasswordUtil.hash(newPassword));
            p.setInt(2, id);
            return p.executeUpdate() > 0;
        } catch (Exception e) {
            AppLog.error("UtilisateurDAO.updatePwd", e);
            return false;
        }
    }

    public static boolean delete(int id) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM utilisateur WHERE id=?")) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        } catch (Exception e) {
            AppLog.error("UtilisateurDAO.delete", e);
            return false;
        }
    }

    private static Utilisateur map(ResultSet r) throws SQLException {
        return new Utilisateur(r.getInt("id"), r.getString("login"), r.getString("mot_de_passe"),
                r.getString("nom"), r.getString("prenom"), r.getString("role"), r.getBoolean("actif"));
    }
}
