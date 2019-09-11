/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.commands;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 *
 * @author eduard
 */
public class LastSeen implements CommandExecutor, TabCompleter{
    String[] connection;
    public LastSeen(String[] connection){
        this.connection=connection;
    }
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        boolean validArgs=false;
        if(args.length==1){
            validArgs = args[0].matches("^[A-Za-z0-9_]{3}[A-Za-z0-9_]*$") && args[0].length()<17;
        }
        if(validArgs){
            String SQL_QUERY = "select * from users_data where user='" + args[0] + "'";
            try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                    PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);
                ){
                ResultSet result = preparedStatement.executeQuery();
                if (result.next()) {
                    String user = result.getString("user");
                    String last_seen = result.getString("last_seen");
                    cs.sendMessage("Player "+user+" was last seen "+last_seen+" GMT.");
                }else{
                    cs.sendMessage("Player "+args[0]+" was not found.");
                }
                preparedStatement.close();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return validArgs;
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] args) {
        List<String> rList = new ArrayList<>();
        if(args.length==1){
            String SQL_QUERY = "select user from users_data where mcuser <> '" + cs.getName() + "'";
            try (Connection conn = DriverManager.getConnection(connection[0] + connection[1] + connection[2], connection[3], connection[4]);
                    PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);) {
                ResultSet result = preparedStatement.executeQuery();
                if (result.next()) {
                    String user = result.getString("user");
                    rList.add(user);
                }
                preparedStatement.close();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rList;
    }
    
}
