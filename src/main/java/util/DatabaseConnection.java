package util;

import java.sql.*;

public class DatabaseConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 5432;
    private static final String DB = "NOTE", USER = "postgres", PASS = "1234";
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

    /**
     * Set the app-level username on a connection for audit trail
     */
    public static void setSessionUser(Connection c, String username) throws SQLException {
        if (username != null && !username.isEmpty()) {
            try (Statement s = c.createStatement()) {
                s.execute("SET gestnotes.username = '" + username.replace("'", "''") + "'");
            }
        }
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
                AppLog.info("Base creee.");
            }
        }
    }

    private static void ensureSchema() throws Exception {
        try (Connection c = DriverManager.getConnection(URL_DB, USER, PASS); Statement s = c.createStatement()) {
            // Tables metier
            s.executeUpdate("CREATE TABLE IF NOT EXISTS etudiant(num_etudiant INTEGER PRIMARY KEY,nom VARCHAR(100) NOT NULL,moyenne REAL NOT NULL DEFAULT 0)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS matiere(num_mat INTEGER PRIMARY KEY,design VARCHAR(100) NOT NULL,coef REAL NOT NULL DEFAULT 1)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS note(num_etudiant INTEGER NOT NULL,num_mat INTEGER NOT NULL,note REAL NOT NULL CHECK(note>=0 AND note<=20),PRIMARY KEY(num_etudiant,num_mat),FOREIGN KEY(num_etudiant) REFERENCES etudiant(num_etudiant) ON DELETE CASCADE,FOREIGN KEY(num_mat) REFERENCES matiere(num_mat) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS audit_note(id SERIAL PRIMARY KEY,type_operation VARCHAR(10) NOT NULL,date_operation TIMESTAMP NOT NULL DEFAULT NOW(),num_etudiant INTEGER NOT NULL,nom VARCHAR(100),design VARCHAR(100),note_ancien REAL,note_nouv REAL,utilisateur VARCHAR(100) NOT NULL DEFAULT 'systeme')");

            // Table utilisateurs
            s.executeUpdate("CREATE TABLE IF NOT EXISTS utilisateur(id SERIAL PRIMARY KEY,login VARCHAR(50) UNIQUE NOT NULL,mot_de_passe VARCHAR(128) NOT NULL,nom VARCHAR(100) NOT NULL,prenom VARCHAR(100) NOT NULL DEFAULT '',role VARCHAR(20) NOT NULL CHECK(role IN('ADMIN','ENSEIGNANT','ETUDIANT')),actif BOOLEAN NOT NULL DEFAULT true,date_creation TIMESTAMP NOT NULL DEFAULT NOW())");

            // Index
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_audit_date ON audit_note(date_operation DESC)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_note_etudiant ON note(num_etudiant)");

            // Trigger: recalcul moyenne
            s.executeUpdate("""
                    CREATE OR REPLACE FUNCTION fn_recalculer_moyenne() RETURNS TRIGGER AS $$
                    DECLARE v INTEGER; m REAL;
                    BEGIN v:=COALESCE(NEW.num_etudiant,OLD.num_etudiant);
                    SELECT COALESCE(SUM(n.note*mt.coef)/NULLIF(SUM(mt.coef),0),0) INTO m FROM note n JOIN matiere mt ON mt.num_mat=n.num_mat WHERE n.num_etudiant=v;
                    UPDATE etudiant SET moyenne=m WHERE num_etudiant=v;
                    IF TG_OP='UPDATE' AND OLD.num_etudiant<>NEW.num_etudiant THEN
                    SELECT COALESCE(SUM(n.note*mt.coef)/NULLIF(SUM(mt.coef),0),0) INTO m FROM note n JOIN matiere mt ON mt.num_mat=n.num_mat WHERE n.num_etudiant=OLD.num_etudiant;
                    UPDATE etudiant SET moyenne=m WHERE num_etudiant=OLD.num_etudiant; END IF; RETURN NULL; END; $$ LANGUAGE plpgsql""");

            // Trigger: audit — uses gestnotes.username session variable (set by app before CUD)
            s.executeUpdate("""
                    CREATE OR REPLACE FUNCTION fn_audit_note() RETURNS TRIGGER AS $$
                    DECLARE vn VARCHAR(100); vd VARCHAR(100); ve INTEGER; vm INTEGER;
                        v_user VARCHAR(100);
                    BEGIN
                        ve:=COALESCE(NEW.num_etudiant,OLD.num_etudiant); vm:=COALESCE(NEW.num_mat,OLD.num_mat);
                        SELECT nom INTO vn FROM etudiant WHERE num_etudiant=ve;
                        SELECT design INTO vd FROM matiere WHERE num_mat=vm;
                        v_user := COALESCE(NULLIF(current_setting('gestnotes.username', true),''), current_user);
                        IF TG_OP='INSERT' THEN INSERT INTO audit_note(type_operation,num_etudiant,nom,design,note_nouv,utilisateur) VALUES('INSERT',ve,vn,vd,NEW.note,v_user);
                        ELSIF TG_OP='UPDATE' THEN INSERT INTO audit_note(type_operation,num_etudiant,nom,design,note_ancien,note_nouv,utilisateur) VALUES('UPDATE',ve,vn,vd,OLD.note,NEW.note,v_user);
                        ELSIF TG_OP='DELETE' THEN INSERT INTO audit_note(type_operation,num_etudiant,nom,design,note_ancien,utilisateur) VALUES('DELETE',ve,vn,vd,OLD.note,v_user);
                        END IF; RETURN NULL;
                    END; $$ LANGUAGE plpgsql""");

            s.executeUpdate("DROP TRIGGER IF EXISTS trg_recalculer_moyenne ON note");
            s.executeUpdate("CREATE TRIGGER trg_recalculer_moyenne AFTER INSERT OR UPDATE OR DELETE ON note FOR EACH ROW EXECUTE FUNCTION fn_recalculer_moyenne()");
            s.executeUpdate("DROP TRIGGER IF EXISTS trg_audit_note ON note");
            s.executeUpdate("CREATE TRIGGER trg_audit_note AFTER INSERT OR UPDATE OR DELETE ON note FOR EACH ROW EXECUTE FUNCTION fn_audit_note()");

            // Default users (insert only if table is empty)
            try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM utilisateur")) {
                r.next();
                if (r.getInt(1) == 0) {
                    String hAdmin = PasswordUtil.hash("admin");
                    String hEns = PasswordUtil.hash("ens123");
                    String hEtu = PasswordUtil.hash("etu123");
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO utilisateur(login,mot_de_passe,nom,prenom,role) VALUES(?,?,?,?,?)")) {
                        p.setString(1, "admin");
                        p.setString(2, hAdmin);
                        p.setString(3, "Administrateur");
                        p.setString(4, "Systeme");
                        p.setString(5, "ADMIN");
                        p.executeUpdate();
                        p.setString(1, "enseignant");
                        p.setString(2, hEns);
                        p.setString(3, "Enseignant");
                        p.setString(4, "Test");
                        p.setString(5, "ENSEIGNANT");
                        p.executeUpdate();
                        p.setString(1, "etudiant");
                        p.setString(2, hEtu);
                        p.setString(3, "Etudiant");
                        p.setString(4, "Test");
                        p.setString(5, "ETUDIANT");
                        p.executeUpdate();
                    }
                    AppLog.info("Utilisateurs par defaut crees: admin/admin, enseignant/ens123, etudiant/etu123");
                }
            }

            AppLog.info("Schema pret (tables, triggers, utilisateurs).");
        }
    }
}
