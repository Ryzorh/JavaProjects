/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eduard.minecraftserver;

import com.eduard.commands.Balance;
import com.eduard.commands.LastSeen;
import com.eduard.commands.Register;
import com.eduard.commands.SetPerms;
import com.eduard.commands.Town;
import com.eduard.commands.Whisper;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author eduard
 */
public class PluginTest extends JavaPlugin {
    @Override
    public void onEnable(){
        String[] connection={"jdbc:mysql://localhost:3306","/Minecraft","?useSSL=false&serverTimezone=UTC", "root", "xx8VQhJl"};
        DataBase.connection=connection;
        DataBase.createDB();
        DataBase.executeUpdate("CREATE TABLE IF NOT EXISTS users_data (mcuser varchar(255) not null, user varchar(255) not null, `group` varchar(255) not null, wealth decimal (65,2) not null, town varchar(255), last_seen datetime not null, register_date datetime not null, pay_date datetime not null,PRIMARY KEY(user))");
        DataBase.executeUpdate("CREATE TABLE IF NOT EXISTS users_trns (mcuser varchar(255) not null, user varchar(255) not null, PRIMARY KEY(mcuser))");
        DataBase.executeUpdate("CREATE TABLE IF NOT EXISTS chunks_town (x INT not null, z INT not null, town_name varchar(255) not null, owner varchar(255) not null, seized tinyint(1) not null, PRIMARY KEY(x,z))");
        DataBase.executeUpdate("CREATE TABLE IF NOT EXISTS towns (mayor varchar(255) not null, town_name varchar(255) not null, seized_days int not null, pvp tinyint(1) not null, rent decimal (65,2) not null, PRIMARY KEY(town_name))");
        SetPerms perms=new SetPerms(this, connection);
        Register register = new Register(connection);
        Balance balance=new Balance(connection);
        LastSeen lastSeen=new LastSeen(connection);
        Whisper whisper = new Whisper(connection);
        Town town =new Town(connection);
        this.getCommand("register").setExecutor(register);
        this.getCommand("register").setTabCompleter(register);
        this.getCommand("bal").setExecutor(balance);
        this.getCommand("bal").setTabCompleter(balance);
        this.getCommand("last_seen").setExecutor(lastSeen);
        this.getCommand("last_seen").setTabCompleter(lastSeen);
        this.getCommand("w").setExecutor(whisper);
        this.getCommand("w").setTabCompleter(whisper);
        this.getCommand("perm").setExecutor(perms);
        this.getCommand("perm").setTabCompleter(perms);
        this.getCommand("town").setExecutor(town);
        this.getServer().getPluginManager().registerEvents(new ListenerTest(perms, connection), this);
        Thread townsThread= new Thread(town);
        townsThread.start();
    }    
    
    @Override
    public void onDisable(){
        System.out.println("PluginTest is dosabled");
    }    
}
