package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
//import java.io.ByteArrayInputStream;
//import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
// import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import crypt.Keys;

public class KnownHosts {

    private static final String URL = "jdbc:sqlite:data/known_peers.db";

    // Constructor
    public KnownHosts() {
        try {
            // Create the table if it doesn't exist
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // connect to database
    private Connection connect() {
        Connection connection = null;
        try {
            // Register SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Try to establish a connection
            connection = DriverManager.getConnection(URL);

        } catch (ClassNotFoundException | SQLException e) {
            // Handle exceptions
            e.printStackTrace();
        }

        if (connection == null) {
            System.err.println("Failed to establish a connection to the database.");
            System.exit(1); // Terminate the program if the connection fails
        }

        return connection;
    }

    // createTable
    // creates table if doesnt exist
    private void createTable() throws SQLException {
        try (Connection connection = connect();
                Statement statement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS known_peers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "public_key TEXT NOT NULL," +
                    "ip_address TEXT NOT NULL)";
            statement.executeUpdate(createTableQuery);
        }
    }

    // insertRecord
    // inserts a new record into table
    public int insertRecord(String name, String publicKey, String ipAddress) {

        // check if record already exists
        if (isNameExists(name) || isIpExists(ipAddress)) {
            return 1;
        }

        try (Connection connection = connect();
                Statement statement = connection.createStatement()) {
            String insertDataQuery = "INSERT INTO known_peers (name, public_key, ip_address) VALUES " +
                    "('" + name + "', '" + publicKey + "', '" + ipAddress + "')";
            statement.executeUpdate(insertDataQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int insertRecord(Host host) {

        if (isNameExists(host.getName())) {
            return 1;
        }

        try (Connection connection = connect();
                Statement statement = connection.createStatement()) {

            String insertDataQuery = "INSERT INTO known_peers (name, public_key, ip_address) VALUES " +
                    "('" + host.getName() + "', '" + host.getPubKey() + "', '" + host.getIp() + "')";
            statement.executeUpdate(insertDataQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Deserialize byte[] to List<Host>
    @SuppressWarnings("unchecked")
    public void mergeDatabase(byte[] hostSerial) {

        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(hostSerial);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);

            // Read the List<Node> from the ObjectInputStream

            for (Host host : (List<Host>) objectStream.readObject()) {

                // check if node exists already
                if (!isNameExists(host.getName())) {

                    // insert if does not exist
                    insertRecord(host);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // check if a name already exists in database
    public boolean isNameExists(String name) {
        try (Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM known_peers WHERE name = ?")) {

            preparedStatement.setString(1, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // check if an ip addr exists in database
    private boolean isIpExists(String ipAddress) {

        try (Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM known_peers WHERE ip_address = ?")) {

            preparedStatement.setString(1, ipAddress);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // look up a certain name
    public String lookupPublicKeyByName(String name) {
        try (Connection connection = connect();
                Statement statement = connection.createStatement()) {
            String query = "SELECT public_key FROM known_peers WHERE name = '" + name + "'";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    return resultSet.getString("public_key");
                } else {
                    return null; // Name not found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // lookup ip address
    public String lookupIPAddressByName(String name) {
        try (Connection connection = connect();
                Statement statement = connection.createStatement()) {
            String query = "SELECT ip_address FROM known_peers WHERE name = '" + name + "'";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next()) {
                    return resultSet.getString("ip_address");
                } else {
                    return null; // Name not found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // get name for pubkey
    public String lookupNameByPublicKey(String publicKey) {
        try (Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT name FROM known_peers WHERE public_key = ?")) {

            preparedStatement.setString(1, publicKey);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lookup public key by IP address
    public String lookupPubKeyByIP(String ipAddress) {
        try (Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT public_key FROM known_peers WHERE ip_address = ?")) {

            preparedStatement.setString(1, ipAddress);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("public_key");
                } else {
                    return null; // IP address not found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lookup name by IP address
    public String lookupNameByIP(String ipAddress) {
        try (Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT name FROM known_peers WHERE ip_address = ?")) {

            preparedStatement.setString(1, ipAddress);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                } else {
                    return null; // IP address not found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // get all rows matching name
    public List<String> getAllMatchingRows(String name) {
        List<String> matchingRows = new ArrayList<>();

        try (Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM known_peers WHERE name = ?")) {

            preparedStatement.setString(1, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String row = String.format("ID: %d, Name: %s, Public Key: %s, IP Address: %s",
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("public_key"),
                            resultSet.getString("ip_address"));
                    matchingRows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return matchingRows;
    }

    // delete row
    public void deleteRowsByName(String name) {
        try (Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "DELETE FROM known_peers WHERE name = ?")) {

            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // read all nodes into a list
    public List<Host> readAllHosts() {
        List<Host> allHosts = new ArrayList<>();

        try (Connection connection = connect();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM known_peers")) {


            while (resultSet.next()) {

                Host host = new Host(
                        resultSet.getString("name"),
                        resultSet.getString("ip_address"),
                        resultSet.getString("public_key"));

                allHosts.add(host);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allHosts;
    }

    public byte[] serialize() {

        // get hosts list
        List<Host> hostList = readAllHosts();

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);

            // write to object stream
            objectStream.writeObject(hostList);

            // get byte array
            return byteStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get Host object by IP address
    public Host getHostByIP(String ipAddress) {
        try (Connection connection = connect();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM known_peers WHERE ip_address = ?")) {

            preparedStatement.setString(1, ipAddress);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String ip = resultSet.getString("ip_address");
                    String publicKey = resultSet.getString("public_key");

                    // return host
                    return new Host(name, ip, publicKey);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Return null if no Host found with the given IP
    }

    // Get all public keys from the database
    public List<PublicKey> getAllPublicKeys() {
        List<PublicKey> publicKeys = new ArrayList<>();

        try {
            List<Host> hosts = readAllHosts(); // Assuming this method retrieves all hosts from the database
            for (Host host : hosts) {
                publicKeys.add(host.getPubKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return publicKeys;
    }

}
