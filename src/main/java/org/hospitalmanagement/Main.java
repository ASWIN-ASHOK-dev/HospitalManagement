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
    private String accessLevel;
    private List<String> commandlistAdmin = new ArrayList<String>();
    private int lastUser;

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

    List<HashMap<String, String>> executeReturn(String command) {
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
    void executeOnly(String command){
        try{
            Statement stmt = conn.createStatement();
            stmt.execute(command);
        }
        catch (SQLException e) {
            System.out.println(e);
        }
    }
    static String inputtaker(){
        String userinput;
        Scanner authenticationscanner = new Scanner(System.in);
        userinput = authenticationscanner.nextLine();
        if(userinput.equalsIgnoreCase("q")){
            System.exit(0);
        }
        return userinput;
    }
    String inputtaker(String print){
        System.out.print(print + " -> " );
        String userinput;
        Scanner authenticationscanner = new Scanner(System.in);
        userinput = authenticationscanner.nextLine();
        if(userinput.equalsIgnoreCase("q")){
            authenticationRequest();
        }
        return userinput;
    }
    void userHandling() {
        System.out.println("Logged in,type h for commands");
        while(true){
            //ADMIN HANDLING
            if(accessLevel.equalsIgnoreCase("Full")) {
                System.out.print("Enter a command");
                String adminInput = inputtaker().toLowerCase();
                switch (adminInput){
                    case "l":
                        authenticationRequest();
                        break;
                    case "n":
                        createUser();
                    case "s":
                        searchUser();
                    case "h":
                        addAdminCommand("l", "logout ");
                        addAdminCommand("nu", "newuser");
                        addAdminCommand("su", "search");
                        helpAdminPrint();
                    default:
                        System.out.println("Wrong command,type h for command list");
                }
            }
        }

    }
    void authenticationRequest(){
        while (true){
            String  userid="";
            String password;
            accessLevel="nil";
            System.out.print("Enter your username -> ");
            boolean registeredUser = false;
            boolean authenticationSuccess=false;
            username = inputtaker();
            if (!username.isBlank()) {
                for (HashMap<String, String> user : executeReturn("select * from userdetails;")) {
                    if (user.get("Name").equalsIgnoreCase(username)) {
                        username = user.get("Name");
                        userid = user.get("id");
                        registeredUser = true;
                        accessLevel = user.get("Accesslevel");
                        break;
                    }
                }
                if(registeredUser){
                    System.out.printf("Welcome %s (id -> %s) %n",username,userid);
                    String storedHashedPassword;
                    List<HashMap<String, String>> a = executeReturn("select password from userdetails where id ="+ userid);
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
            if(authenticationSuccess) {
                System.out.println("Your Access Level is "+accessLevel);
                break;
            }
        }
    }
    String returncondition(HashMap<String,String> conditionlist){
        List<String> conditionCommand=new ArrayList<>();
        for (Map.Entry<String,String> condition: conditionlist.entrySet()){
            if(!condition.getValue().isBlank()){
                conditionCommand.add(String.format("%s = '%s'",condition.getKey(),condition.getValue()));
            }
        }
        return String.join("AND ",conditionCommand);
    }
    //Methods
    void createUser(){
        String username;
        String password = "";
        String department;
        String accesslevel;
        System.out.println("------USER CREATION MODE-----");
        System.out.println("press q to log out");
        username = inputtaker("Enter the username");
        password = inputtaker("Enter the password");
        department = inputtaker("Enter the Department");
        accesslevel = inputtaker("Enter Access level W/R/Nil/Full");
        inputtaker(String.format("you are about to add a user %n Name:%s,Department:%s,Access level:%s,Password:%s %n Press enter to continue,q to logout",username,department,accesslevel,password));
        SHA256HASH hashedpassword = new SHA256HASH(password);
        password =  hashedpassword.returnHash();
        lastUser = Integer.parseInt(executeReturn("select id from userdetails").getLast().get("id"));
        String sqlcommand = String.format("insert into userdetails(id,Name,Department,Accesslevel,password) values ('%s','%s','%s','%s','%s');",lastUser + 1,username,department,accesslevel,password);
        executeOnly(sqlcommand);
    }
    void searchUser(){
        System.out.println("------USER SEARCH MODE-----");
        HashMap<String,String> conditionList= new HashMap<String,String>();
        System.out.println("press q to log out, leave blank if not needed to search based on that");
        conditionList.put("id",inputtaker("Enter the userid"));
        conditionList.put("Name",inputtaker("Enter the name").toUpperCase());
        conditionList.put("Department",inputtaker("Enter the Department").toUpperCase());
        conditionList.put("Accesslevel",inputtaker("Enter Access level W/R/Nil/Full").toUpperCase());
        String condition = returncondition(conditionList);
        List<HashMap<String,String>> output;
        if (condition.isEmpty()){
            output = executeReturn("select * from userdetails;");
        }
        else {
            output = executeReturn("select * from userdetails where " + condition);
        }
        System.out.println("Search Results");
        for(HashMap individualoutput : output)
        {
            StringBuilder finaloutput = new StringBuilder();
            finaloutput.append("id = " + individualoutput.get("id"));
            finaloutput.append(" -- Name = " + individualoutput.get("Name"));
            finaloutput.append(" -- Department = " + individualoutput.get("Department"));
            System.out.println(finaloutput);

        }
    }
    void helpAdminPrint() {
        for(int i = 0;i < commandlistAdmin.size();i++){
            System.out.println("Sl No. "+Integer.toString(i + 1)+ " " + commandlistAdmin.get(i));
        }
    }
    void addAdminCommand(String letter,String use){
        commandlistAdmin.add(" --> command letter --> "+letter + " --> use -->" + use);
    }
}
class Info{
    static final String hospitalName = "AK Hospital";
}
class Main{
    public static void main(String[] args){
        SQLconnection sqlConnection = new SQLconnection("aswin","psupsc","JAVA");
        sqlConnection.Connect();
        System.out.println("Welcome to Hospital Management System of " + Info.hospitalName);
        System.out.println("----------------------------------------------");
        sqlConnection.authenticationRequest();
        sqlConnection.userHandling();
    }
}

