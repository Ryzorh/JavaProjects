/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.mysqltest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduard
 */
public class Main {
    private static ResultSet executeQuery(String SQL_QUERY){
        try (
            Connection conn= DriverManager.getConnection("jdbc:mysql://localhost:3306/Minecraft?useSSL=false&serverTimezone=GMT", "root", "o0x79ybl");
            PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet resultSet=preparedStatement.executeQuery();
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    private static void createDB(){
        String SQL_STMNT = "CREATE DATABASE IF NOT EXISTS Minecraft CHARACTER SET utf8mb4 ";
        try (
            Connection conn= DriverManager.getConnection("jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC", "root", "o0x79ybl");
            PreparedStatement preparedStatement = conn.prepareStatement(SQL_STMNT);){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void createTable(){
        String SQL_STMNT = "CREATE TABLE IF NOT EXISTS UsersData (user varchar(255) not null, wealth double precision(10,2) not null, PRIMARY KEY(user))";
        try (
            Connection conn= DriverManager.getConnection("jdbc:mysql://localhost:3306/Minecraft?useSSL=false&serverTimezone=UTC", "root", "o0x79ybl");
            PreparedStatement preparedStatement = conn.prepareStatement(SQL_STMNT);){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void executeUpdate(String SQL_STMNT){
        try (
            Connection conn= DriverManager.getConnection("jdbc:mysql://localhost:3306/Minecraft?useSSL=false&serverTimezone=UTC", "root", "o0x79ybl");
            PreparedStatement preparedStatement = conn.prepareStatement(SQL_STMNT);){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
            /*
            createDB();
            createTable();
            executeUpdate("INSERT INTO UsersData (user, wealth) VALUES ('Ryzorh', 10000000)");
            */
        try {
            String SQL_QUERY;
            PreparedStatement preparedStatement;
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Minecraft?useSSL=false&serverTimezone=GMT", "root", "o0x79ybl");
            SQL_QUERY="select * from users_data where user=(select user from users_trns where mcuser='Ryzorh')";
            preparedStatement = conn.prepareStatement(SQL_QUERY);
            ResultSet nResults = preparedStatement.executeQuery();
            if(nResults.next()){
                System.out.println(nResults.getString("last_seen"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
