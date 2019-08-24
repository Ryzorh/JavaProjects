/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.minecraftserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduard
 */
public class DataBase {
    static String[] connection;
    public static void createDB(){
        String SQL_STMNT = "CREATE DATABASE IF NOT EXISTS Minecraft CHARACTER SET utf8mb4 ";
        try (
                Connection conn = DriverManager.getConnection(connection[0]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_STMNT);){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void executeUpdate(String SQL_STMNT){
        try(
            Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
            PreparedStatement preparedStatement = conn.prepareStatement(SQL_STMNT);){
        preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQL statement failed "+SQL_STMNT);
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
}
