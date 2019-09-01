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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author eduard
 */
public class SetPerms implements CommandExecutor, TabCompleter{
    private HashMap<UUID,PermissionAttachment> playerAttachments;
    private JavaPlugin plugin;
    String[] connection;
    public SetPerms(JavaPlugin plugin, String[] connection){
        playerAttachments=new HashMap<>();
        this.plugin=plugin;
        this.connection=connection;
    }
    public void addPlayerAttachment(Player player){
        PermissionAttachment permissionAttachment=player.addAttachment(plugin);
        playerAttachments.put(player.getUniqueId(), permissionAttachment);                
    }
    public void removePlayerAttachment(Player player){
        PermissionAttachment permissionAttachment=playerAttachments.get(player.getUniqueId());
        player.removeAttachment(permissionAttachment);
    }
    public void addGroupPermissions(String group, Player player){
        PermissionAttachment permissionAttachment=playerAttachments.get(player.getUniqueId());
        permissionAttachment.setPermission(group, true);
    }
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        if (!cs.hasPermission("plugintest.perm") && !cs.isOp()) {
            cs.sendMessage("You haven't got clearence to execute this command.");
            return false;
        }
        boolean validArgs = false;
        String[] groups={"Newcomer", "Member", "Admin"};
        if (args.length == 2) {
            boolean isInArray=false;
            for (String group : groups) {
                isInArray = isInArray || args[0].equals(group);
            }
            validArgs = isInArray && args[1].matches("^[A-Za-z0-9]*$");
        }
        if (validArgs) {
            String SQL_QUERY = "select * from users_data where user='" + args[1] + "'";
            try (Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);
                ) {
                ResultSet result = preparedStatement.executeQuery();
                if (result.next()) {
                    String mcuser = result.getString("mcuser");
                    String user = result.getString("user");
                    Player player= Bukkit.getPlayer(mcuser);
                    if(player==null) return false;
                    if(!player.isOnline()) return false;
                    addGroupPermissions(args[0],player);
                    DataBase.executeUpdate("update users_data set `group`= '"+args[0]+"' where user='"+user+"'");
                    player.sendMessage(args[0]+" permissions have been given to you.");
                } else {
                    cs.sendMessage("Player " + args[1] + " was not found.");
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
            rList.add("Newcomer");
            rList.add("Member");
            rList.add("Admin");
        }else if(args.length==2){
            String SQL_QUERY = "select * from users_data where mcuser <> '" + cs.getName() + "'";
            try (Connection conn = DriverManager.getConnection(connection[0] + connection[1] + connection[2], connection[3], connection[4]);
                    PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);) {
                ResultSet result = preparedStatement.executeQuery();
                if (result.next()) {
                    String user = result.getString("user");
                    String mcuser = result.getString("mcuser");
                    Player player= Bukkit.getPlayer(mcuser);
                    if(player!=null) if(player.isOnline()) rList.add(user);
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
