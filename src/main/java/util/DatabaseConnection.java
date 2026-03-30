package util;

import java.sql.*;

public class DatabaseConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 5432;
    private static final String DB = "NOTE", USER = "postgres", PASS = "Fanilost*40";
    private static final String URL_DB = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB;
    private static final String URL_PG = "jdbc:postgresql://" + HOST + ":" + PORT + "/postgres";
    private static volatile boolean init = false;

    public static Connection getConnection() throws Exception {
        if (!init) {
            synchronized (DatabaseConnection.class) {
                if (!init) {
                    ensureDb();
                    ensureSchema();
                    init = true;
                }
            }
        }
        return DriverManager.getConnection(URL_DB, USER, PASS);
    }

    private static void ensureDb() throws Exception {
        try (Connection a = DriverManager.getConnection(URL_PG, USER, PASS)) {
            boolean exists;
            try (PreparedStatement p = a.prepareStatement("SELECT 1 FROM pg_database WHERE datname=?")) {
                p.setString(1, DB);
                try (ResultSet r = p.executeQuery()) {
                    exists = r.next();
                }
            }
            if (!exists) {
                try (Statement s = a.createStatement()) {
                    s.executeUpdate("CREATE DATABASE \"" + DB + "\" ENCODING 'UTF8'");
                }
                AppLog.info("Base \"" + DB + "\" creee.");
            } else {
                AppLog.info("Base \"" + DB + "\" presente.");
            }
        }
    }

    private static void ensureSchema() throws Exception {
        try (Connection c = DriverManager.getConnection(URL_DB, USER, PASS); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS etudiant (num_etudiant INTEGER PRIMARY KEY, nom VARCHAR(100) NOT NULL, moyenne REAL NOT NULL DEFAULT 0)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS matiere (num_mat INTEGER PRIMARY KEY, design VARCHAR(100) NOT NULL, coef REAL NOT NULL DEFAULT 1)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS note (num_etudiant INTEGER NOT NULL, num_mat INTEGER NOT NULL, note REAL NOT NULL CHECK(note>=0 AND note<=20), PRIMARY KEY(num_etudiant,num_mat), FOREIGN KEY(num_etudiant) REFERENCES etudiant(num_etudiant) ON DELETE CASCADE, FOREIGN KEY(num_mat) REFERENCES matiere(num_mat) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS audit_note (id SERIAL PRIMARY KEY, type_operation VARCHAR(10) NOT NULL, date_operation TIMESTAMP NOT NULL DEFAULT NOW(), num_etudiant INTEGER NOT NULL, nom VARCHAR(100), design VARCHAR(100), note_ancien REAL, note_nouv REAL, utilisateur VARCHAR(50) NOT NULL DEFAULT 'admin')");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_audit_date ON audit_note(date_operation DESC)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_note_etudiant ON note(num_etudiant)");
            s.executeUpdate("""
                    CREATE OR REPLACE FUNCTION fn_recalculer_moyenne() RETURNS TRIGGER AS $$
                    DECLARE v INTEGER; m REAL;
                    BEGIN v:=COALESCE(NEW.num_etudiant,OLD.num_etudiant);
                    SELECT COALESCE(SUM(n.note*mt.coef)/NULLIF(SUM(mt.coef),0),0) INTO m FROM note n JOIN matiere mt ON mt.num_mat=n.num_mat WHERE n.num_etudiant=v;
                    UPDATE etudiant SET moyenne=m WHERE num_etudiant=v;
                    IF TG_OP='UPDATE' AND OLD.num_etudiant<>NEW.num_etudiant THEN
                    SELECT COALESCE(SUM(n.note*mt.coef)/NULLIF(SUM(mt.coef),0),0) INTO m FROM note n JOIN matiere mt ON mt.num_mat=n.num_mat WHERE n.num_etudiant=OLD.num_etudiant;
                    UPDATE etudiant SET moyenne=m WHERE num_etudiant=OLD.num_etudiant; END IF; RETURN NULL; END; $$ LANGUAGE plpgsql""");
            s.executeUpdate("""
                    CREATE OR REPLACE FUNCTION fn_audit_note() RETURNS TRIGGER AS $$
                    DECLARE vn VARCHAR(100); vd VARCHAR(100); ve INTEGER; vm INTEGER;
                    BEGIN ve:=COALESCE(NEW.num_etudiant,OLD.num_etudiant); vm:=COALESCE(NEW.num_mat,OLD.num_mat);
                    SELECT nom INTO vn FROM etudiant WHERE num_etudiant=ve; SELECT design INTO vd FROM matiere WHERE num_mat=vm;
                    IF TG_OP='INSERT' THEN INSERT INTO audit_note(type_operation,num_etudiant,nom,design,note_nouv,utilisateur) VALUES('INSERT',ve,vn,vd,NEW.note,current_user);
                    ELSIF TG_OP='UPDATE' THEN INSERT INTO audit_note(type_operation,num_etudiant,nom,design,note_ancien,note_nouv,utilisateur) VALUES('UPDATE',ve,vn,vd,OLD.note,NEW.note,current_user);
                    ELSIF TG_OP='DELETE' THEN INSERT INTO audit_note(type_operation,num_etudiant,nom,design,note_ancien,utilisateur) VALUES('DELETE',ve,vn,vd,OLD.note,current_user);
                    END IF; RETURN NULL; END; $$ LANGUAGE plpgsql""");
            s.executeUpdate("DROP TRIGGER IF EXISTS trg_recalculer_moyenne ON note");
            s.executeUpdate("CREATE TRIGGER trg_recalculer_moyenne AFTER INSERT OR UPDATE OR DELETE ON note FOR EACH ROW EXECUTE FUNCTION fn_recalculer_moyenne()");
            s.executeUpdate("DROP TRIGGER IF EXISTS trg_audit_note ON note");
            s.executeUpdate("CREATE TRIGGER trg_audit_note AFTER INSERT OR UPDATE OR DELETE ON note FOR EACH ROW EXECUTE FUNCTION fn_audit_note()");
            AppLog.info("Schema pret.");
        }
    }
}
