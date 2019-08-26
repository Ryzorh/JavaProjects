/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.commands;

import com.eduard.minecraftserver.DataBase;
import com.eduard.minecraftserver.ListenerTest;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author eduard
 */
public class Town implements CommandExecutor, Runnable{
    String[] connection;
    double chunkProtectionCost;
    HashMap <String,  String> invitation;
    public Town (String[] connection){
        this.connection=connection;
        this.chunkProtectionCost=5.0;
        invitation=new HashMap<>();
    }
    private boolean occupiedChunk(int x, int z) {
        String SQL_QUERY = "select * from chunks_town where x=" + x + " and z=" + z;
        try (Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);) {
            ResultSet result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            Logger.getLogger(ListenerTest.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }
    private boolean pay(String user, BigDecimal wealth, BigDecimal expense){
        boolean payed=false;
        if (wealth.subtract(expense).compareTo(new BigDecimal(0)) != -1) {
            DataBase.executeUpdate("update users_data set wealth=" + (wealth.subtract(expense)) + " where user='" + user + "'");
            payed = true;
        }
        return payed;
    }
    private boolean insertNewTown(CommandSender cs, String user, String townName, BigDecimal wealth){
        boolean successfull= false;
        if(pay(user, wealth, new BigDecimal(50000))){
            String SQL_STMNT = "INSERT INTO towns (mayor, town_name, seized_days, pvp,public, rent, tax) values ('" + user + "', '" + townName + "', 30, 0, 0, 0, 0)";
            try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                    PreparedStatement preparedStatement = conn.prepareStatement(SQL_STMNT);) {
                preparedStatement.executeUpdate();
                cs.sendMessage("Your town has been registered succesfully.");
                successfull=true;
                DataBase.executeUpdate("update users_data set town='" + townName + "' where user='" + user + "'");
            } catch (SQLIntegrityConstraintViolationException sQLICVE) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, sQLICVE);
                cs.sendMessage("The town already exists.");
                successfull= false;                
            } catch (SQLException ex) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                successfull= false;
            }
        }else{
            cs.sendMessage("You haven't got money enough. It costs $50,000.");
        }
        return successfull;
    }
    private boolean newTown(CommandSender cs, String townName){
        boolean successfull=false;
        String SQL_QUERY= "select * from users_data where user=(select user from users_trns where mcuser='"+cs.getName()+"')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String town = result.getString("town");
                if (!result.wasNull()) {
                    cs.sendMessage("You already belong to town.");
                    return false;
                }
                String user = result.getString("user");
                BigDecimal wealth = result.getBigDecimal("wealth");
                successfull= insertNewTown(cs, user, townName, wealth);
            } else {
                cs.sendMessage("Please, register yourself.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean doesTownExist(String townName){
        boolean exist=false;
        String SQL_QUERY= "select * from towns where town_name='"+townName+"' and public=1";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            exist=result.next();
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return exist;
    }
    private boolean joinTown(CommandSender cs, String townName){
        boolean successfull=false;
        String SQL_QUERY= "select * from users_data where user=(select user from users_trns where mcuser='"+cs.getName()+"')";
        try (  Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String town = result.getString("town");
                if (!result.wasNull()) {
                    cs.sendMessage("You already belong to town.");
                    return false;
                }
                if(doesTownExist(townName)){
                    String user = result.getString("user");
                    DataBase.executeUpdate("update users_data set town='"+townName+"' where user='"+user+"'");
                    successfull= true;
                }else{
                    cs.sendMessage("The town doesn't exist or it is not public.");
                }
            } else {
                cs.sendMessage("Please, register yourself.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private String getMayor(String townName){
        String mayor=null;
        String SQL_QUERY = "select mayor from towns where town_name='" + townName+ "'";
        try (Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);) {
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                mayor=result.getString("mayor");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mayor;
    }
    private boolean leaveTown(CommandSender cs){
        boolean successfull=false;
        String SQL_QUERY= "select * from users_data where user=(select user from users_trns where mcuser='"+cs.getName()+"')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String town = result.getString("town");
                if (result.wasNull()) {
                    cs.sendMessage("You don't belong to any town.");
                }else{
                    successfull=true;
                    String user = result.getString("user");
                    String mayor=getMayor(town);
                    if(mayor.equals(user)){
                        DataBase.executeUpdate("update users_data set town=null where town='"+town+"'");
                        DataBase.executeUpdate("delete from towns where town_name='"+town+"'");
                    }else{
                        DataBase.executeUpdate("update users_data set town=null where user='"+user+"'");
                    }
                }
            } else {
                cs.sendMessage("Please, register yourself.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean setPVP(CommandSender cs){
        boolean successfull=false;
        String SQL_QUERY= "select * from users_data ud join towns t on (ud.town=t.town_name) where user=(select user from users_trns where mcuser='"+cs.getName()+"')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String town = result.getString("town");
                String mayor = result.getString("mayor");
                String user = result.getString("user");
                boolean pvp = result.getBoolean("pvp");
                if (mayor.equals(user)) {
                    DataBase.executeUpdate("update towns set pvp=" + (pvp ? 0 : 1) + " where town_name='" + town + "'");
                    successfull = true;
                } else {
                    cs.sendMessage("This option can only be set by the mayor of your town.");
                }
            } else {
                cs.sendMessage("You are not registered or you don't belong to any town.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean setPublic(CommandSender cs){
        boolean successfull=false;
        String SQL_QUERY= "select * from users_data ud join towns t on (ud.town=t.town_name) where user=(select user from users_trns where mcuser='"+cs.getName()+"')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String town = result.getString("town");
                String mayor = result.getString("mayor");
                String user = result.getString("user");
                boolean pblc = result.getBoolean("public");
                if (mayor.equals(user)) {
                    DataBase.executeUpdate("update towns set public=" + (pblc ? 0 : 1) + " where town_name='" + town + "'");
                    successfull = true;
                } else {
                    cs.sendMessage("This option can only be set by the mayor of your town.");
                }
            } else {
                cs.sendMessage("You are not registered or you don't belong to any town.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean setRent(CommandSender cs, BigDecimal rent){
        boolean successfull=false;
        String SQL_QUERY= "select * from users_data ud join towns t on (ud.town=t.town_name) where user=(select user from users_trns where mcuser='"+cs.getName()+"')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String town = result.getString("town");
                String mayor = result.getString("mayor");
                String user = result.getString("user");
                if (mayor.equals(user)) {
                    DataBase.executeUpdate("update users_data u join towns t on u.town=t.town_name set t.rent=" + rent + " where u.user=(select ut.user from users_trns ut where ut.mcuser='" + cs.getName() + "') and t.mayor=u.user");
                    successfull = true;
                } else {
                    cs.sendMessage("This option can only be set by the mayor of your town.");
                }
            } else {
                cs.sendMessage("You are not registered or you don't belong to any town.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean setTax(CommandSender cs, BigDecimal tax){
        boolean successfull=false;
        String SQL_QUERY= "select * from users_data ud join towns t on (ud.town=t.town_name) where user=(select user from users_trns where mcuser='"+cs.getName()+"')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String town = result.getString("town");
                String mayor = result.getString("mayor");
                String user = result.getString("user");
                if (mayor.equals(user)) {
                    DataBase.executeUpdate("update users_data u join towns t on u.town=t.town_name set t.tax=" + tax + " where u.user=(select ut.user from users_trns ut where ut.mcuser='" + cs.getName() + "') and t.mayor=u.user");
                    successfull = true;
                } else {
                    cs.sendMessage("This option can only be set by the mayor of your town.");
                }
            } else {
                cs.sendMessage("You are not registered or you don't belong to any town.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean setSeizedDays(CommandSender cs, int seizedDays){
        boolean successfull=false;
        String SQL_QUERY= "select * from users_data ud join towns t on (ud.town=t.town_name) where user=(select user from users_trns where mcuser='"+cs.getName()+"')";
        try (   Connection conn = DriverManager.getConnection(connection[0]+connection[1]+connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);){
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String town = result.getString("town");
                String mayor = result.getString("mayor");
                String user = result.getString("user");
                if (mayor.equals(user)) {
                    DataBase.executeUpdate("update users_data u join towns t on u.town=t.town_name set t.seizedDays=" +seizedDays+ " where u.user=(select ut.user from users_trns ut where ut.mcuser='" + cs.getName() + "') and t.mayor=u.user");
                    successfull = true;
                } else {
                    cs.sendMessage("This option can only be set by the mayor of your town.");
                }
            } else {
                cs.sendMessage("You are not registered or you don't belong to any town.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean getTownInfo(CommandSender cs, String town){
        boolean successfull = false;
        String SQL_QUERY = "select * from towns where town_name='"+town+"'";
        try (Connection conn = DriverManager.getConnection(connection[0] + connection[1] + connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);) {
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                String mayor = result.getString("mayor");
                boolean pvp = result.getBoolean("pvp");
                boolean pblc = result.getBoolean("public");
                BigDecimal rent = result.getBigDecimal("rent");
                BigDecimal tax = result.getBigDecimal("tax");
                int seizedDays = result.getInt("seized_days");
                String[] message={
                    "Town: "+town,
                    "Mayor: "+mayor,
                    "PVP: "+pvp,
                    "Public: "+pblc,
                    "Rent: $"+rent,
                    "Tax: $"+tax,
                    "Days before expropriation: "+seizedDays
                };
                cs.sendMessage(message);
                successfull=true;
            } else {
                cs.sendMessage("The town does not exist.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean sendInvitation(CommandSender cs, String user, String town){
        boolean successfull=false;
        String SQL_QUERY = "select * from users_data ud join towns t on (ud.town=t.town_name) where user='" + user+ "' and ud.town is null";
        try (Connection conn = DriverManager.getConnection(connection[0] + connection[1] + connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);) {
            ResultSet result = preparedStatement.executeQuery();
            if(result.next()){
                String mcuser=result.getString("mcuser");
                Player player=Bukkit.getPlayer(mcuser);
                if(player!=null){
                    if(player.isOnline()){
                        player.sendMessage("You have been invited to join "+town);
                        invitation.put(mcuser,town);
                        successfull=true;
                    }else{
                        cs.sendMessage("The player is not available.");
                    }
                }else {
                    cs.sendMessage("The player is not available.");
                }
                successfull=true;
            }else{
                cs.sendMessage("The user doesn't exist or he/she already belongs to a town.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Town.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    private boolean invite(CommandSender cs, String user){
        boolean successfull=false;
        String SQL_QUERY = "select * from users_data ud join towns t on (ud.town=t.town_name) where user=(select user from users_trns where mcuser='"+cs.getName()+"') and t.mayor=ud.user";
        try (Connection conn = DriverManager.getConnection(connection[0] + connection[1] + connection[2], connection[3], connection[4]);
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_QUERY);) {
            ResultSet result = preparedStatement.executeQuery();
            if(result.next()){
                String town = result.getString("town");
                successfull=sendInvitation(cs, user, town);
            }else{
                cs.sendMessage("You are not the mayor of any town.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Town.class.getName()).log(Level.SEVERE, null, ex);
        }
        return successfull;
    }
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        if (!cs.hasPermission("plugintest.town") && !cs.isOp()) {
            cs.sendMessage("You haven't got clearence to execute this command.");
            return false;
        }
        boolean success = false;
        if(args.length>0){
            switch(args[0]){
                case "new":
                    if(args.length==2){
                        if(args[1].matches("^[A-Za-z0-9]*$")){
                            success=newTown(cs, args[1]);
                        }
                    }
                    break;
                case "info":
                    if(args.length==2){
                        if(args[1].matches("^[A-Za-z0-9]*$")){
                            success=getTownInfo(cs, args[1]);
                        }
                    }
                    break;
                case "join":
                    if(args.length==2){
                        if(args[1].matches("^[A-Za-z0-9]*$")){
                            success=joinTown(cs, args[1]);
                        }
                    }
                    break;
                case "leave": 
                    if(args.length==1){
                        success=leaveTown(cs);
                    }
                    break;
                case "pvp": 
                    if(args.length==1){
                        success=setPVP(cs);
                    }
                    break;
                case "public": 
                    if(args.length==1){
                        success=setPublic(cs);
                    }
                    break;
                case "rent": 
                    if(args.length==2){
                        if(args[1].matches("^[0-9][0-9]*$")){
                            BigDecimal rent=new BigDecimal(args[1]);
                            success=setRent(cs, rent);
                        }
                    }                        
                    break;
                case "tax": 
                    if(args.length==2){
                        if(args[1].matches("^[0-9][0-9]*$")){
                            BigDecimal tax=new BigDecimal(args[1]);
                            success=setTax(cs, tax);
                        }
                    }                        
                    break;
                case "seized_days": 
                    if(args.length==2){
                        if(args[1].matches("^[0-9][0-9]*$")){
                            int seizedDays=Integer.parseInt(args[1]);
                            success=setSeizedDays(cs, seizedDays);
                        }
                    }                        
                    break;
                case "invite": 
                    if (args[1].matches("^[A-Za-z0-9]*$")) {
                        success = invite(cs,args[1]);
                    }
                    break;
                case "accept": 
                    String town = invitation.get(cs.getName());
                    if(town!=null){
                        DataBase.executeUpdate("update users_data set t.town='" +town+ "' where u.user=(select ut.user from users_trns ut where ut.mcuser='" + cs.getName() + "')");
                        invitation.remove(cs.getName());
                        success=true;
                    }else{
                        cs.sendMessage("You have not got any invitation.");
                    }
                    break;
                case "claim": 
                    break;
                case "abandon": 
                    break;
            }
        }
        return success;
    }

    @Override
    public void run() {
        while(true){
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Calendar cal = Calendar.getInstance();
                DataBase.executeUpdate(""
                        + "update users_data u inner join towns t on (u.town=t.town_name) "
                        + "set u.wealth="
                            + "if(date(u.pay_date)<>date(STR_TO_DATE('"+dateFormat.format(cal.getTime())+"','%Y/%m/%d %H:%i:%s')), "
                                + "if(u.user=t.mayor, u.wealth-(select count(*)*"+chunkProtectionCost+" from chunks_town c2 where u.town=c2.town_name), u.wealth-t.rent*(select count(*) from chunks_town c2 where u.user=c2.owner)), "
                                + "u.wealth"
                            + "), "
                        + "u.pay_date="
                            + "if(date(u.pay_date)<>date(STR_TO_DATE('"+dateFormat.format(cal.getTime())+"','%Y/%m/%d %H:%i:%s')), "
                                + "STR_TO_DATE('"+dateFormat.format(cal.getTime())+"','%Y/%m/%d %H:%i:%s'), u.pay_date"
                            + ") "
                );
                DataBase.executeUpdate(""
                        + "update users_data u inner join chunks_town c on u.user=c.owner "
                        + "set c.seized=if(u.wealth<0, 1, 0)");
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Town.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
