package org.hospitalmanagement;
import java.sql.*;
import java.util.Scanner;

class SQLconnection {
    String username;
    String password;
    String databaseurl;
    Connection conn;

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

    void Execute(String command) {
        try {
            System.out.println(conn);
            Statement stmt = conn.createStatement();
            ResultSet rset =stmt.executeQuery(command);
            while (rset.next()) {
                System.out.println(rset.getInt("id"));
            }
            rset.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error connecting to DB: " + e.getMessage());
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
        Scanner inputTaker = new Scanner(System.in);
        try{
            String userinput = inputTaker.nextLine();
            System.out.println(userinput);
        sqlConnection.Execute(userinput.strip());
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}

