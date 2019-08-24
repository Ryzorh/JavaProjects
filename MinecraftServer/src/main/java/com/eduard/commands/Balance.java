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
public class Balance implements CommandExecutor ,TabCompleter{
    String[] connection;
    public Balance(String[] connection){
        this.connection=connection;
    }
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        String SQL_QUERY = "select * from users_data where user=(select user from users_trns where mcuser='" + cs.getName() + "')";
        if (args.length==0){
            try (Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);
                ) {
                ResultSet result = preparedStatement.executeQuery();
                if (result.next()) {
                    double wealth = result.getDouble("wealth");
                    String user = result.getString("user");
                    cs.sendMessage("Player "+user+" owns $"+wealth+".");
                }
                preparedStatement.close();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return args.length==0;
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] strings) {
        return new ArrayList<>();
    }
}
