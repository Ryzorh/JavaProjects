/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.first;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author eduard
 */
public class Main extends JPanel implements Runnable{
    public String globalVariable;
    static JLabel label;
    public Main(BufferedImage bI){
        super(new BorderLayout());
        label= new JLabel(new ImageIcon(bI));
        this.add(label);
        globalVariable="helloo";
        Prova prova=new Prova(this);
        prova.setGlobalVariable();
        System.out.println(globalVariable);
    }
    private boolean[][][] generateMaze(int lado) throws InterruptedException{
        boolean[][][] result=new boolean[lado][lado][2];
        Set <Coords> visited=new HashSet<>();
        ArrayList<int[]> path = new ArrayList<>();
        for(int i=0; i<result.length; i++){
            for(int j=0; j<result.length; j++){
                result[i][j][0]=i!=lado-1;
                result[i][j][1]=j!=lado-1;
            }
        }
        Random rnd = new Random(48472);
        int start =(int)Math.floor(rnd.nextDouble()*lado*4);
        result[start/lado<2?start%lado:start/lado==2?lado-1:0][start/lado>1?start%lado:start/lado==1?lado-1:0][start/lado<2?0:1]=false;
        int[] point={start/lado<2?start%lado:start/lado==2?lado-1:0,start/lado>1?start%lado:start/lado==1?lado-1:0};
        point[0]=point[0]>lado -2? lado-2:point[0];
        point[1]=point[1]>lado -2? lado-2:point[1];
        visited.add(new Coords(point[0],point[1]));
        path.add(point);
        int[][] dir={{0,1},{1,0},{0,-1},{-1,0}};
        while(visited.size()<(lado-1)*(lado-1)){
            ArrayList<Integer> nList = new ArrayList<>();
            for(int i=0; i<dir.length; i++){
                int[] newStep={point[0]+dir[i][0], point[1]+dir[i][1]};
                int[] preStep=path.get(path.size()-1);
                
                int[] wall = {i != 1 ? point[0] : point[0] + 1, i != 0 ? point[1] : point[1] + 1};
                int wdir = i == 1 || i == 3 ? 1 : 0;
                
                if(newStep[0]<lado -1 && newStep[0]>=0 && 
                        newStep[1]<lado -1 && newStep[1]>=0 &&
                        (newStep[0]!=preStep[0] || newStep[1]!= preStep[1]) &&
                        !visited.contains(new Coords(newStep[0],newStep[1]))){
                    nList.add(i);
                }
            }
            int[] newPos=new int[2];
            if(!nList.isEmpty()){
                int random = nList.get((int)Math.floor(nList.size()*rnd.nextDouble()));
                newPos[0]=point[0]+dir[random][0];
                newPos[1]=point[1]+dir[random][1];
                
                int[] wall = {random != 1 ? point[0] : point[0] + 1, random != 0 ? point[1] : point[1] + 1};
                int wdir = random == 1 || random == 3 ? 1 : 0;

                result[wall[0]][wall[1]][wdir]= false;
                visited.add(new Coords(newPos[0],newPos[1]));
                path.add(newPos);
            }else{
                newPos[0]=path.get(path.size()-1)[0];
                newPos[1]=path.get(path.size()-1)[1];
                path.remove(path.size()-1);
            }
            label.setIcon(new ImageIcon(buildMaze(result)));
            point=newPos;
        }
        /*
        int end = (int) Math.floor(rnd.nextDouble() * lado * 4);
        result[end / lado < 2 ? end % lado : end / lado == 2 ? lado - 1 : 0][end / lado > 1 ? end % lado : end / lado == 1 ? lado - 1 : 0][end / lado < 2 ? 0 : 1] = false;
        label.setIcon(new ImageIcon(buildMaze(result)));
        */
        return result;
    }
    private void save(boolean[][][] maze){
        FileWriter fW= null;
        PrintWriter pW = null;
        try {
            fW = new FileWriter(new File("maze.txt"));
            pW=new PrintWriter(fW);
            for (int i=0; i< maze.length-1; i++){
                String line="";
                for(int j=0; j< maze[i].length-1; j++){
                    line+=maze[i][j][1]?"W W W W W W W ":"W A A A A A A ";
                }
                line+="W ";
                pW.println(line);
                for (int k=0; k<6; k++){
                    line = "";
                    for (int j = 0; j < maze[0].length - 1; j++) {
                        line += maze[i][j][0] ? "W A A A A A A " : "A A A A A A A ";
                    }
                    line += maze[i][maze[i].length - 1][0] ? "W " : "A ";
                    pW.println(line);
                }
            }
            String line = "";
            for (int j = 0; j < maze[maze.length-1].length - 1; j++) {
                line+=maze[maze.length-1][j][1]?"W W W W W W W ":"W A A A A A A ";
            }
            line += "W ";
            pW.println(line);

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                pW.close();
                fW.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void save2(boolean[][][] maze){
        FileWriter fW= null;
        PrintWriter pW = null;
        try {
            fW = new FileWriter(new File("maze2.txt"));
            pW=new PrintWriter(fW);
            for (int i=0; i< maze.length-1; i++){
                String line="";
                for(int j=0; j< maze[i].length-1; j++){
                    line+=maze[i][j][1]?"W W ":"W A ";
                }
                line+="W ";
                pW.println(line);
                line = "";
                for (int j = 0; j < maze[0].length - 1; j++) {
                    line += maze[i][j][0] ? "W A " : "A A ";
                }
                line += maze[i][maze[i].length - 1][0] ? "W " : "A ";
                pW.println(line);
            }
            String line = "";
            for (int j = 0; j < maze[maze.length-1].length - 1; j++) {
                line+=maze[maze.length-1][j][1]?"W W ":"W A ";
            }
            line += "W ";
            pW.println(line);

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                pW.close();
                fW.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private BufferedImage buildMaze(boolean[][][] maze){
        BufferedImage bI=new BufferedImage(512,512,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = bI.createGraphics();
        g2.setColor(Color.black);
        g2.fillRect(0, 0, bI.getWidth(), bI.getHeight());
        g2.setColor(Color.green);
        Random rnd = new Random(1);
        int[][] r ={{1,0},{0,1}} ;
        for(int i=0; i<maze.length; i++){
            for(int j=0; j<maze[i].length; j++){
                for(int k=0; k<r.length; k++){
                    if(maze[i][j][k]){
                        g2.drawLine(7*i, 7*j, 7*(i+r[k][0]), 7*(j+r[k][1]));
                    }
                }
            }
        }
        g2.dispose();
        return bI;
    }
    private BufferedImage paintMaze128x128(boolean[][][] maze){
        BufferedImage bI=new BufferedImage(147,147,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = bI.createGraphics();
        g2.setColor(Color.black);
        g2.fillRect(0, 0, bI.getWidth(), bI.getHeight());
        g2.setColor(Color.green);
        int[][] r ={{1,0},{0,1}} ;
        for(int i=0; i<maze.length; i++){
            for(int j=0; j<maze[i].length; j++){
                for(int k=0; k<r.length; k++){
                    if(maze[i][j][k]){
                        g2.drawLine(2*i, 2*j, 2*(i+r[k][0]), 2*(j+r[k][1]));
                    }
                }
            }
        }
        g2.dispose();
        BufferedImage bI2=new BufferedImage(128,128,BufferedImage.TYPE_4BYTE_ABGR);
        double[] dx = {(double) bI.getWidth() / (double) bI2.getWidth(),(double) bI.getHeight() / (double) bI2.getHeight()};
        double[] x={0,0};
        double total= Math.floor(dx[0]+1)*Math.floor(dx[1]+1);
        for(int i=0; i<bI2.getWidth(); i++){
            for(int j=0; j<bI2.getHeight(); j++){
                double rgb=0;
                for(int n=0; n<(int)Math.floor(dx[0])+1; n++){
                    for(int m=0; m<(int)Math.floor(dx[1])+1; m++){
                        if((int)Math.floor(x[0])+n<bI.getWidth() && (int)Math.floor(x[1])+m<bI.getHeight()){
                            double color=(double)bI.getRGB((int)Math.floor(x[0])+n, (int)Math.floor(x[1])+m);
                            rgb+=(n==0?x[0]-Math.floor(x[0]):1.0)*(m==0?x[1]-Math.floor(x[1]):1.0)*color/total;
                        }
                    }
                }
                System.out.print(rgb);
                bI2.setRGB(i, j, (int)rgb);
                x[1]+=dx[1];
            }
            System.out.println();
            x[1]=0;
            x[0]+=dx[0];
        }
        return bI2;
    }
    private static void createAndShowGUI(){
        JFrame frame = new JFrame("Labyrinth");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Main main=new Main(new BufferedImage(512,512,BufferedImage.TYPE_4BYTE_ABGR));
        JComponent contentPane=main;
        contentPane.setOpaque(true);
        frame.setContentPane(contentPane);
        
        frame.pack();
        frame.setVisible(true);
        
        Thread t= new Thread(main);
        t.start();
    }
    public static void main(String[] args){
        /*
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar cal = Calendar.getInstance();
        System.out.println(dateFormat.format(cal.getTime()));
        String[] ids = TimeZone.getAvailableIDs();
        for(int i=0; i< ids.length; i++){
            System.out.println(ids[i]);
        }
        System.out.println("kJ899;UJJI".matches("^[A-Za-z0-9]*$"));
        */
        
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
        
    }
    private static void saveImage(BufferedImage bI, String file){
        try {
            File outputfile = new File(file);
            ImageIO.write(bI, "png", outputfile);
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    @Override
    public void run() {
        try {
            boolean[][][] maze =generateMaze(74);
            saveImage(buildMaze(maze), "512x512.png");
            saveImage(paintMaze128x128(maze), "128x128.png");
            save(maze);
            save2(maze);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
