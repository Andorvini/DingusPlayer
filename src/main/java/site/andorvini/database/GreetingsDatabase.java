package site.andorvini.database;

import site.andorvini.Main;

import java.sql.*;

public class GreetingsDatabase {

    private static String databasePath = "jdbc:sqlite:data/database.db";

    public static void checkExist() {
        Connection connection = null;
        Statement databaseStatement = null;

        try {
            connection = DriverManager.getConnection(databasePath);
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(databasePath);
            databaseStatement = connection.createStatement();

            String greetingTableQuery = "CREATE TABLE IF NOT EXISTS greetings (server_id INTEGER, user_id INTEGER, url TEXT)";

            databaseStatement.execute(greetingTableQuery);

            databaseStatement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addUrl(Long userId, Long serverId, String url) {
        String query = "INSERT INTO greetings (server_id, user_id, url) VALUES (?, ?, ?)";
        try {
                Connection connection = DriverManager.getConnection(databasePath);
                PreparedStatement preparedStatement = connection.prepareStatement(query);

                preparedStatement.setLong(1, serverId);
                preparedStatement.setLong(2, userId);
                preparedStatement.setString(3, url);

                preparedStatement.execute();

                connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkIfGreetingExists(Long userId, Long serverId) {
        try {
            String url = null;
            Connection connection = DriverManager.getConnection(databasePath);
            Statement statement = connection.createStatement();
            ResultSet result;

            String query = "SELECT url FROM greetings WHERE user_id = '" + userId + "' AND server_id = '" + serverId +"'";

            result = statement.executeQuery(query);

            while (result.next()) {
                url = result.getString("url");
            }

            connection.close();
            statement.close();
            result.close();

            if (url == null) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getGreetingUrl(Long serverId, Long userId) {
        String url = null;

        try {
            Connection connection = DriverManager.getConnection(databasePath);
            Statement statement = connection.createStatement();
            ResultSet result;

            String query = "SELECT url FROM greetings WHERE user_id = '" + userId + "' AND server_id = '" + serverId +"'";

            result = statement.executeQuery(query);

            while (result.next()) {
                url = result.getString("url");
            }

            connection.close();
            statement.close();
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    public static void removeGreeting(Long serverId, Long userId) {
        try {
            Connection connection = DriverManager.getConnection(databasePath);
            Statement statement = connection.createStatement();

            String query = "DELETE FROM greetings WHERE user_id = '" + userId + "' AND server_id = '" + serverId +"'";

            statement.execute(query);

            connection.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}












