package dao;

import model.Etudiant;
import util.AppLog;
import util.DatabaseConnection;

import java.sql.*;
import java.util.*;

public class StatsDAO {
    public static int countEtudiants() {
        return cnt("etudiant");
    }

    public static int countMatieres() {
        return cnt("matiere");
    }

    public static int countNotes() {
        return cnt("note");
    }

    public static int countAudit() {
        return cnt("audit_note");
    }

    public static float moyenneGenerale() {
        try (Connection c = DatabaseConnection.getConnection(); ResultSet r = c.createStatement().executeQuery("SELECT COALESCE(AVG(moyenne),0) FROM etudiant WHERE moyenne>0")) {
            if (r.next()) return r.getFloat(1);
        } catch (Exception e) {
            AppLog.error("StatsDAO.moyenne", e);
        }
        return 0;
    }

    public static List<Etudiant> topEtudiants(int lim) {
        List<Etudiant> l = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT * FROM etudiant WHERE moyenne>0 ORDER BY moyenne DESC LIMIT ?")) {
            p.setInt(1, lim);
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    l.add(new Etudiant(r.getInt("num_etudiant"), r.getString("nom"), r.getFloat("moyenne")));
            }
        } catch (Exception e) {
            AppLog.error("StatsDAO.top", e);
        }
        return l;
    }

    public static Map<String, Integer> noteDistribution() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("0-5", 0);
        m.put("5-8", 0);
        m.put("8-10", 0);
        m.put("10-12", 0);
        m.put("12-14", 0);
        m.put("14-16", 0);
        m.put("16-20", 0);
        try (Connection c = DatabaseConnection.getConnection(); ResultSet r = c.createStatement().executeQuery("SELECT note FROM note")) {
            while (r.next()) {
                float n = r.getFloat("note");
                String b = n < 5 ? "0-5" : n < 8 ? "5-8" : n < 10 ? "8-10" : n < 12 ? "10-12" : n < 14 ? "12-14" : n < 16 ? "14-16" : "16-20";
                m.merge(b, 1, Integer::sum);
            }
        } catch (Exception e) {
            AppLog.error("StatsDAO.distrib", e);
        }
        return m;
    }

    public static float tauxReussite() {
        try (Connection c = DatabaseConnection.getConnection(); ResultSet r = c.createStatement().executeQuery(
                "SELECT COUNT(*) FILTER(WHERE moyenne>=10) AS ok, COUNT(*) AS tot FROM etudiant WHERE moyenne>0")) {
            if (r.next()) {
                int tot = r.getInt("tot");
                return tot > 0 ? (float) r.getInt("ok") / tot * 100 : 0;
            }
        } catch (Exception e) {
            AppLog.error("StatsDAO.taux", e);
        }
        return 0;
    }

    private static int cnt(String t) {
        try (Connection c = DatabaseConnection.getConnection(); ResultSet r = c.createStatement().executeQuery("SELECT COUNT(*) FROM " + t)) {
            if (r.next()) return r.getInt(1);
        } catch (Exception e) {
            AppLog.error("StatsDAO.cnt " + t, e);
        }
        return 0;
    }
}
