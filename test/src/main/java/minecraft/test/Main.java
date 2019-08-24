/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minecraft.test;

import com.mojang.nbt.ByteArrayTag;
import com.mojang.nbt.ByteTag;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.DoubleTag;
import com.mojang.nbt.FloatTag;
import com.mojang.nbt.IntTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.LongArrayTag;
import com.mojang.nbt.LongTag;
import com.mojang.nbt.NbtIo;
import com.mojang.nbt.ShortTag;
import com.mojang.nbt.StringTag;
import com.mojang.nbt.Tag;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.world.level.chunk.storage.RegionFile;

/**
 *
 * @author Eduard
 */
public class Main {
    public static void main(String[] args){
        
        //String baseFolder = "C:\\Users\\Eduard\\MinecraftServers\\Server02\\world";
        //String baseFolder = "C:\\Users\\Eduard\\AppData\\Roaming\\.minecraft\\saves\\minimum";
        /*
        String baseFolder = "/home/eduard/MinecraftServers/Server01/world";
        Manager manager = new Manager();
        for (int i = 0; i < 512; i++) {
            for (int j = 0; j < 512; j++) {
                if((i-255)*(i-255)+(j-255)*(j-255)<255*255 && (i-255)*(i-255)+(j-255)*(j-255)>247*247){
                    int section=manager.getHeighestSection(baseFolder, i, j);
                    section=section+3<16?section+3:15;
                    Coordinate coord = new Coordinate(i, 0, j);
                    Block block = new Block("minecraft:bedrock");
                    manager.putBlock(coord, block);
                    for(int k=1; k<section*16; k++){
                        coord = new Coordinate(i, k, j);
                        block = new Block("minecraft:quartz_block");
                        manager.putBlock(coord, block);
                    }
                }
            }
        }
        manager.save(baseFolder);
        
        */
        /* 
        MazeBuilder mB=new MazeBuilder();
        try {
            mB.openChunkFiles(new File("/home/eduard/MinecraftServers/Server01/world/region/r.0.0.mca"), getMaze());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            //RegionFile rF= new RegionFile(new File("C:\\Users\\Eduard\\MinecraftServers\\Server02\\world\\region\\r.0.0.mca"));
            //RegionFile rF= new RegionFile(new File("C:\\Users\\Eduard\\AppData\\Roaming\\.minecraft\\saves\\minimum\\region\\r.0.0.mca"));
            RegionFile rF= new RegionFile(new File("/home/eduard/MinecraftServers/Server01/world/region/r.0.0.mca.mod"));
            new File("output.txt").delete();
            readChunk(rF,1,12);
            //readFile(rF);
            rF.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        readMap();
        setMap();
    }
    private static void setMap(){
        File inputFile=new File("/home/eduard/MinecraftServers/Server01/world/data/map_0.dat");
        File outputFile = new File("/home/eduard/MinecraftServers/Server01/world/data/map_0.dat.mod");
        try {
            CompoundTag root = NbtIo.readCompressed(new FileInputStream(inputFile));
            CompoundTag data = root.getCompound("data");
            data.putInt("zCenter", 256);
            data.putInt("xCenter", 256);
            //root.put("data", data);

            NbtIo.writeCompressed(root, new FileOutputStream(outputFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void readMap(){
        new File("output.txt").delete();
        File dataFile=new File("/home/eduard/MinecraftServers/Server01/world/data/map_3.dat");
        try {
            CompoundTag root = NbtIo.readCompressed(new FileInputStream(dataFile));
            readLevel(root, 0);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
                
    }
    private static String[][] getMaze(){
        String[][] result=null;
        try {
            FileReader fR=new FileReader(new File("maze.txt"));
            Scanner scn = new Scanner(fR);
            ArrayList<String[]> map = new ArrayList<>();
            while(scn.hasNext()){
                String line=scn.nextLine();
                String[] maze=line.split(" ");
                map.add(maze);
            }
            scn.close();
            fR.close();
            result=new String[map.size()][];
            for(int i=0; i<map.size(); i++){
                result[i]=map.get(i);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    private static void readChunk(RegionFile rF, int i, int j) throws IOException{
        if (rF.hasChunk(i, j)) {
            DataInputStream regionChunkInputStream = rF.getChunkDataInputStream(i, j);
            if (regionChunkInputStream == null) {
                regionChunkInputStream.close();
                return;
            }
            CompoundTag tagOld = NbtIo.read(regionChunkInputStream);
            regionChunkInputStream.close();
            readLevel(tagOld, 0);
        }
    }
    private static void readFile(RegionFile rF){
        try {
            for (int i=0; i<32; i++){
                for (int j=0; j<32; j++){
                    readChunk(rF,i,j);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void readLevel(CompoundTag parentTag, int n){
        String preTab = "";
        for (int i = 0; i < n; i++) {
            preTab += "\t";
        }
        FileWriter fW = null;
        PrintWriter pW= null;
        try {
            fW = new FileWriter(new File("output.txt"), true);
            pW=new PrintWriter(fW);
            pW.println(preTab+parentTag.getName()+"(C)");
            pW.close();
            fW.close();
            Iterator allTags=parentTag.getAllTags().iterator();
            while(allTags.hasNext()){
                Tag tag=(Tag) allTags.next();
                readTag(tag,n+1, parentTag);
            }
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
    private static void readTag(Tag tag, int n, CompoundTag parentTag){
        try {
            String preTab = "";
            for (int i = 0; i < n; i++) {
                preTab += "\t";
            }
            FileWriter fW=new FileWriter(new File("output.txt"),true);
            PrintWriter pW=new PrintWriter(fW);
            switch (tag.getId()) {
                case Tag.TAG_Compound:
                    pW.close();
                    fW.close();
                    readLevel((CompoundTag) tag, n);
                    break;
                case Tag.TAG_Byte_Array:
                    pW.println(preTab + tag.getName() + "(ba)");
                    ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
                    if("colors".equals(tag.getName())){
                        byte[] data= byteArrayTag.data;
                        pW.println(preTab + data.length);
                        for (byte tmp: data){
                            pW.print(tmp+" ");
                        }
                    }
                    break;
                case Tag.TAG_Double:
                    DoubleTag doubleTag = (DoubleTag) tag;
                    pW.println(preTab + tag.getName() + "(d)(" + doubleTag.data + ")");
                    break;
                case Tag.TAG_End:
                    pW.println(preTab + tag.getName() + "(END)");
                    break;
                case Tag.TAG_Float:
                    FloatTag floatTag = (FloatTag) tag;
                    pW.println(preTab + tag.getName() + "(f)(" + floatTag.data + ")");
                    break;
                case Tag.TAG_Int:
                    IntTag intTag = (IntTag) tag;
                    pW.println(preTab + tag.getName() + "(i)(" + intTag.data + ")");
                    break;
                case Tag.TAG_Int_Array:
                    pW.println(preTab + tag.getName() + "(ia)");
                    break;
                case Tag.TAG_List:
                    pW.println(preTab + tag.getName() + "(list)");
                    pW.close();
                    fW.close();
                    ListTag<Tag> list = (ListTag<Tag>)tag;
                    for (int i = 0; i < list.size(); i++) {
                        readTag(list.get(i),n+1, parentTag);
                    }
                    break;
                case Tag.TAG_Long:
                    LongTag longTag = (LongTag) tag;
                    pW.println(preTab + tag.getName() + "(l)(" + longTag.data + ")");
                    break;
                case Tag.TAG_Long_Array:
                    pW.println(preTab + tag.getName() + "(la)");
                    if("BlockStates".equals(tag.getName())){
                        LongArrayTag longArrayTag = (LongArrayTag) tag;
                        int bLen=4;
                        int b111=0;
                        for(int i=4; i<32; i++){
                            if(Math.pow(2, i)>=parentTag.getList("Palette").size()){
                                bLen=i;
                                b111=getB111(bLen);
                                break;
                            }
                        }
                        int lLen=64;
                        int line=0;
                        int pos=0;
                        long[] data=longArrayTag.data;
                        for(int i=0; i<16; i++){
                            pW.println("Y="+i+", "+data.length+", "+bLen+", "+b111+", "+bLen%4);
                            for(int j=0; j<16; j++){
                                for(int k=0; k<16; k++){
                                    int cLen = lLen - pos;
                                    int c111 = getB111(cLen);
                                    int dLen = bLen - cLen;
                                    int d111 = getB111(dLen);
                                    long block=pos+bLen<=lLen?(data[line]>>pos)&b111:((data[line]>>pos)&c111)|((data[line+1]&d111)<<cLen);
                                    pW.print(block+"; ");
                                    line=pos+bLen<lLen?line:line+1;
                                    pos=pos+bLen<lLen?pos+bLen:bLen-(lLen-pos);
                                }
                                pW.println();
                            }
                        }
                    }
                    break;
                case Tag.TAG_Short:
                    ShortTag shortTag = (ShortTag) tag;
                    pW.println(preTab + tag.getName() + "(s)(" + shortTag.data + ")");
                    break;
                case Tag.TAG_String:
                    StringTag stringTag = (StringTag) tag;
                    pW.println(preTab + tag.getName() + "(str)(" + stringTag.data + ")");
                    break;
                case Tag.TAG_Byte: 
                    ByteTag byteTag=(ByteTag)tag;
                    pW.println(preTab + tag.getName() + "(b)(" + byteTag.data + ")");
                    break;
            }
            pW.close();
            fW.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static int getB111(int bLen){
        int b111 = 0;
        switch (bLen % 4) {
            case 1:
                b111 = 1;
                break;
            case 2:
                b111 = 3;
                break;
            case 3:
                b111 = 7;
                break;
        }
        for (int j = 0; j < bLen / 4; j++) {
            b111 = (b111 << (j + 1) * 4) | 0xf;
        }
        return b111;
    }
}
