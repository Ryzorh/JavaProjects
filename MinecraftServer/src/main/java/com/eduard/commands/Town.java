/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.commands;

import com.eduard.minecraftserver.DataBase;
import com.eduard.minecraftserver.ListenerTest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author eduard
 */
public class Town implements CommandExecutor, Runnable{
    String[] connection;
    double chunkProtectionCost;
    public Town (String[] connection){
        this.connection=connection;
        this.chunkProtectionCost=5.0;
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
            String SQL_STMNT = "INSERT INTO towns (mayor, town_name, seized_days, pvp, rent) values ('" + user + "', '" + townName + "', 30, 0, 0)";
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
        String SQL_QUERY= "select * from towns where town_name='"+townName+"'";
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
                    cs.sendMessage("The town doesn't exist.");
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
                if (result.wasNull()) {
                    cs.sendMessage("You don't belong to any town.");
                }else{
                    String mayor=result.getString("mayor");
                    String user=result.getString("user");
                    boolean pvp=result.getBoolean("pvp");
                    if (mayor.equals(user)) {
                        DataBase.executeUpdate("update towns set pvp="+(pvp?0:1)+" where town_name='" + town + "'");
                        successfull=true;
                    }else{
                        cs.sendMessage("This option can only be set by the mayor of your town.");
                    }
                }
            } else {
                cs.sendMessage("You are not registered or you don't belong to any town.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Balance.class.getName()).log(Level.SEVERE, null, ex);
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
                //Thread.sleep(3600000);
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Town.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
