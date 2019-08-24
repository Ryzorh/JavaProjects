/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.minecraftserver;

import com.eduard.commands.SetPerms;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author eduard
 */
public class ListenerTest implements Listener{
    SetPerms perms;
    String[] connection;
    public ListenerTest(SetPerms perms, String[] connection){
        this.perms=perms;
        this.connection=connection;
    }
    private void setLastSeen(Player player){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar cal = Calendar.getInstance();
        
        String SQL_QUERY;
        PreparedStatement preparedStatement;
        try (Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4])){
            SQL_QUERY = "select * from users_data where user=(select user from users_trns where mcuser='" + player.getName() + "')";
            preparedStatement = conn.prepareStatement(SQL_QUERY);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String user = result.getString("user");
                DataBase.executeUpdate("update users_data set last_seen=STR_TO_DATE('" + dateFormat.format(cal.getTime()) + "','%Y/%m/%d %H:%i:%s') where user='" + user + "'");
            }
            preparedStatement.close();
        
        } catch (SQLException ex) {
            Logger.getLogger(ListenerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private boolean canBuild(int x, int z, String user){
        String SQL_QUERY = "select * from chunks_town where x=" + x + " and z=" + z;
        try(    Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if(result.next()){
                String owner=result.getString("owner");
                return owner.equals(user);
            }
            return true;
        }catch (SQLException e){
            Logger.getLogger(ListenerTest.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        perms.removePlayerAttachment(event.getPlayer());
        setLastSeen(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        perms.addPlayerAttachment(event.getPlayer());
        String SQL_QUERY;
        SQL_QUERY = "select * from users_data where user=(select user from users_trns where mcuser='" + event.getPlayer().getName() + "')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String user=result.getString("user");
                //Set last seen time.
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Calendar cal = Calendar.getInstance();
                DataBase.executeUpdate("update users_data set last_seen=STR_TO_DATE('" + dateFormat.format(cal.getTime()) + "','%Y/%m/%d %H:%i:%s') where user='" + user + "'");
                //Set join message.
                event.setJoinMessage("Welcome "+user+".");
                //Attach player to plugin to manage hs/her permissions.
                String group= result.getString("group");
                //Set permissions.
                String[] groups={"Newcomer", "Member", "Admin"};
                boolean isInArray = false;
                for (String group1 : groups) {
                    isInArray = isInArray || group.equals(group1);
                }
                if(isInArray) perms.addGroupPermissions(group, event.getPlayer());
                
            }else{
                event.setJoinMessage("Welcome. Please register yourself using the /register command.");
            }
            preparedStatement.close();
            conn.close();
            
        } catch (SQLException ex) {
            event.setJoinMessage("An error happened logging you in. Please, contact with phoerst@outlook.es.");
        }
    }
    @EventHandler
    public void onBuild(BlockCanBuildEvent event){
        String SQL_QUERY= "select * from users_data where user=(select user from users_trns where mcuser='" + event.getPlayer().getName() + "')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);
            ){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                int x = event.getBlock().getChunk().getX();
                int z = event.getBlock().getChunk().getZ();
                String user = result.getString("user");
                event.setBuildable(canBuild(x, z, user));
            } else {
                event.setBuildable(false);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ListenerTest.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        String SQL_QUERY = "select * from users_data where user=(select user from users_trns where mcuser='" + event.getPlayer().getName() + "')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);
            ){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                int x=event.getBlock().getChunk().getX();
                int z=event.getBlock().getChunk().getZ();
                String user = result.getString("user");
                if(canBuild(x,z,user)){
                    BigDecimal wealth=result.getBigDecimal("wealth").add(new BigDecimal(10));
                    DataBase.executeUpdate("update users_data set wealth="+(wealth)+" where user='"+user+"'");
                }else{
                    event.setCancelled(true);
                }
            } else{
                event.setCancelled(true);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ListenerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player && event.getEntity() instanceof Player){
            Player attacker=(Player) event.getDamager();
            Player defender=(Player) event.getEntity();
            String SQL_QUERY = "select * from users_data ud join towns t on (ud.town=t.town_name) where user in (select user from users_trns where mcuser in ('" + attacker + "', '" + defender + "'))";
            try (Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                    PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);) {
                ResultSet result = preparedStatement.executeQuery();
                String town=null;
                if (result.next()) {
                    town=result.getString("town");
                    if (result.next()) {
                        if (result.getString("town").equals(town) && !result.getBoolean("pvp")) {
                            event.setCancelled(true);
                        }
                    }
                }
                preparedStatement.close();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ListenerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    @EventHandler
    public void onSendMessage(AsyncPlayerChatEvent event){
        String SQL_QUERY = "select * from users_data where user=(select user from users_trns where mcuser='" + event.getPlayer().getName() + "')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);
            ){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String user=result.getString("user");
                event.setFormat(user+": "+event.getMessage());
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ListenerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
