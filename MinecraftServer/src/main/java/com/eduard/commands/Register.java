/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.commands;

import com.eduard.minecraftserver.DataBase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
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
public class Register implements CommandExecutor, TabCompleter{
    String[] connection;
    public Register(String[] connection){
        this.connection=connection;
    }
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        boolean validArgs=false;
        if(args.length==1){
            validArgs=args[0].matches("^[A-Za-z0-9_]{3}[A-Za-z0-9_]*$") && args[0].length()<17;
        }
        if(validArgs){
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Calendar cal = Calendar.getInstance();
            
            try (Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4])){
                conn.setAutoCommit(false);
                PreparedStatement preparedStatement;
                String SQL_STMNT;
                
                SQL_STMNT="INSERT INTO users_data (mcuser, user, wealth, `group`, last_seen, register_date, pay_date) VALUES ('"+cs.getName()+"','"+args[0]+"', 0, 'Newcomer', STR_TO_DATE('"+dateFormat.format(cal.getTime())+"','%Y/%m/%d %H:%i:%s'),STR_TO_DATE('"+dateFormat.format(cal.getTime())+"','%Y/%m/%d %H:%i:%s'), STR_TO_DATE('"+dateFormat.format(cal.getTime())+"','%Y/%m/%d %H:%i:%s'))";
                preparedStatement = conn.prepareStatement(SQL_STMNT);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                                
                conn.commit();
                
                cs.sendMessage("You have been registered successfully.");
            } catch (SQLIntegrityConstraintViolationException e) {
                cs.sendMessage("The user name is not available or you are already registered.");
            } catch (SQLException ex) {
                Logger.getLogger(Register.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return validArgs;
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] strings) {
        return new ArrayList<>();
    }
    
}
