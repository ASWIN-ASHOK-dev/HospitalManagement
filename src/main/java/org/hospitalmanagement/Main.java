package org.hospitalmanagement;

//import statements
import java.io.Console;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

class SHA256HASH {
    private String hashedtext;
    SHA256HASH(String wordToHash) {
        this.hashedtext = wordToHash;
    }
    String returnHash(){
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            byte[] hashBytes = messageDigest.digest(hashedtext.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            hashedtext = hexString.toString();
            System.out.println(hashedtext);
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("No Algorithm Found"+e);
        }
        return hashedtext;
    }
}
class SQLconnection {
    private String username;
    private String password;
    private String databaseurl;
    private Connection conn;

    SQLconnection(String username, String password, String database) {
        System.out.println("SQL connection initiated");
        this.username = username;
        this.password = password;
        this.databaseurl = "jdbc:mysql://localhost:3306/" + database;
    }

    SQLconnection() {
        System.out.println("Please provide username, password and database name");
    }

    void Connect() {
        try {
            conn = DriverManager.getConnection(databaseurl, username, password);
            System.out.println("Connected to DB");

        } catch (SQLException e) {
            System.err.println("Error connecting to DB: " + e.getMessage());
        }
    }

    List<HashMap<String, String>> execute(String command) {
        List<HashMap<String, String>> executionResult = new ArrayList<HashMap<String, String>>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(command);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<String, String>();
                for (int i = 1; i <= columnCount; i++) {
                    String id = metaData.getColumnName(i);
                    String value = resultSet.getObject(i).toString();
                    row.put(id, value);
                }
                executionResult.add(row);
            }
            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error connecting to DB: " + e.getMessage());
        }
        return executionResult;
    }
    String inputtaker(){
        String userinput;
        Scanner authenticationscanner = new Scanner(System.in);
        userinput = authenticationscanner.nextLine();
        if(userinput.equalsIgnoreCase("q")){
            System.exit(0);
        }
        return userinput;
    }
    void authenticationRequest() {
        while (true){
            String username;
            String  userid="";
            String password;
            System.out.print("Enter your username -> ");
            boolean registeredUser = false;
            boolean authenticationSuccess=false;
            username = inputtaker();
            if (!username.isBlank()) {
                for (HashMap<String, String> user : execute("select * from userdetails;")) {
                    if (user.get("Name").equalsIgnoreCase(username)) {
                        username = user.get("Name");
                        userid = user.get("id");
                        registeredUser = true;
                        break;
                    }
                }
                if(registeredUser){
                    System.out.printf("Welcome %s (id -> %s) %n",username,userid);
                    String storedHashedPassword;
                    List<HashMap<String, String>> a = execute("select password from userdetails where id ="+ userid);
                    storedHashedPassword =a.getFirst().get("password");
                    if(storedHashedPassword!=null){
                        Console console = System.console();

                        SHA256HASH hash = new SHA256HASH(String.valueOf(console.readPassword("Enter you password,not visible because of security issues")));
                        if (hash.returnHash().equals(storedHashedPassword)){
                            System.out.println("Password Correct");
                            System.out.println("Authentication Completed Successfully");
                            authenticationSuccess = true;
                        }
                        else {
                            System.out.println("Incorrect password,please try again");
                        }
                    }
                    else {
                        System.out.println("Password is empty which is very unlikely to happen please call someone with admin preference");
                        System.out.println("Also please stop re-entering the password");
                    }

                }
                else {
                    System.out.println("user not registered please request someone with admin privilege to add you as a user");
                }
            }
            else{
                System.out.println("please type a username");
            }
            if(authenticationSuccess) break;
        }
    }
}
class Info{
    static String hospitalName;
}
class Main{
    public static void main(String[] args){
        Info.hospitalName = "AK Hospital";
        SQLconnection sqlConnection = new SQLconnection("aswin","psupsc","JAVA");
        sqlConnection.Connect();
        System.out.println("Welcome to Hospital Management System of " + Info.hospitalName);
        System.out.println("----------------------------------------------");
        sqlConnection.authenticationRequest();
    }
}

