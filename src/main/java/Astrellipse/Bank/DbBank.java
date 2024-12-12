package Astrellipse.Bank;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.Arrays;

public class DbBank {
    //接続
    Connection connection;
    public DbBank(JavaPlugin plugin) {
        //フォルダ作成
        File sqliteFolder = plugin.getDataFolder();
        if (!sqliteFolder.exists()) {
            sqliteFolder.mkdirs();
        }
        try {
            //接続　終了時に.close ファイルは指定すると勝手に作ってくれる
            connection = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+"\\"+getClass().getPackageName()+".db");
            //セッション？終了時に.close
            Statement stmt = connection.createStatement();
            //コマンド送信
            stmt.execute("CREATE TABLE IF NOT EXISTS nyan ('key' TEXT, 'value' INTEGER)");
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection closeConnection() {
        return connection;
    }

    //SQLインジェクション対策されてるらしいプレースホルダとやらを使った物
    ///SQL1
    int dget(String k) {
        //小文字
        String key = k.toLowerCase();
        String selectQuery = "SELECT value FROM nyan WHERE key=?";
        String insertQuery = "INSERT INTO nyan (key, value) VALUES (?, ?)";
        try (
                PreparedStatement selectStmt = connection.prepareStatement(selectQuery)
        ) {
            selectStmt.setString(1, key);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("value");
                }
            }
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, key);
                insertStmt.setInt(2, 0);
                insertStmt.executeUpdate();
            }
            return 0;
        } catch (SQLException e) {
            Bukkit.getLogger().warning(Arrays.toString(e.getStackTrace()));
            return -1;
        }
    }

    //値の上書き
    //SQL2
    void drep(String k, int value) {
        //小文字
        String key = k.toLowerCase();
        String updateQuery = "UPDATE nyan SET value = ? WHERE key = ?";
        String insertQuery = "INSERT INTO nyan (key, value) VALUES (?, ?)";
        try {
            // UPDATEクエリを実行
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, value);
                updateStmt.setString(2, key);
                int rowsAffected = updateStmt.executeUpdate(); // 更新された行数を取得

                // 行が更新されなかった場合（キーが存在しない場合）はINSERTクエリを実行
                if (rowsAffected == 0) {
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, key);
                        insertStmt.setInt(2, value);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //加算
    int dadd(String key, int add) {
        int i = dget(key)+add;
        drep(key,i);
        return i;
    }

    //減算
    //マイナスになる場合計算結果だけ返して変更しない
    int drem(String key, int remove) {
        int def = dget(key);
        int res = def-remove;
        if (res <= 0) {
            return res;
        }
        drep(key,res);
        return res;
    }
}
