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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.world.level.chunk.storage.RegionFile;

/**
 *
 * @author Eduard
 */
public class main {
    
    public static void main(String[] args){
            String baseFolder="/home/eduard/MinecraftServers/Server01/world";
            setBlock(baseFolder,0,200,0,new Block("minecraft:diamond_ore"));
            RegionFile rF= new RegionFile(new File("/home/eduard/MinecraftServers/Server01/world/region/r.0.0.mod"));
            new File("output.txt").delete();
            readFile(rF);
    }
    private static void setBlockInSectionChunk(Block block, CompoundTag section, int x, int y,int z){
        boolean foundInPalette = false;
        int posInPalette = 0;
        ListTag<CompoundTag> palette = (ListTag<CompoundTag>) section.getList("Palette");
        for (int j = 0; j < palette.size(); j++) {
            posInPalette = palette.get(j).getString("Name").equals(block.name) ? j : posInPalette;
            foundInPalette = foundInPalette || palette.get(j).getString("Name").equals(block.name);
        }
        if (!foundInPalette) {
            CompoundTag palElem = new CompoundTag();
            palElem.putString("Name", block.name);
            palette.add(palElem);
            posInPalette = palette.size() - 1;
        }
        int xInChunk = x % 16;
        int zInChunk = z % 16;
        int yInChunk = y % 16;

        int bLen = 4;
        int b111 = 0;
        for (int k = 4; k < 32; k++) {
            if (Math.pow(2, k) >= palette.size()) {
                bLen = k;
                b111 = getB111(bLen);
                break;
            }
        }
        int posInArray = (xInChunk + 16 * zInChunk + 16 * 16 * yInChunk) * bLen;
        long mold = (b111 << posInArray % 64);

        long[] blockStates = section.getLongArray("BlockStates");
        long data = blockStates[posInArray / 64];
        long newData = (data & ~mold) | (posInPalette << posInArray % 64);
        blockStates[posInArray / 64] = newData; 
    }
    private static void setBlock(String folder,int x, int y, int z, Block block){
        String rFolder=folder+"/region";
        int cx=(x/16)%32;
        int rx=cx/32;
        int cz=(z/16)%32;
        int rz=cz/32;
        File fileInput= new File(rFolder+"/r."+rx+"."+rz+".mca");
        File fileOutput=new File(rFolder+"/r."+rx+"."+rz+".mod");
        if(fileInput.exists()){
            RegionFile rF= new RegionFile(fileInput);
            RegionFile nRF=new RegionFile(fileOutput);
            try {
                for(int ci=0; ci<32; ci++){
                    for(int cj=0; cj<32; cj++){
                        if (rF.hasChunk(ci, cj)) {
                            DataInputStream regionChunkInputStream = rF.getChunkDataInputStream(ci, cj);
                            if (regionChunkInputStream == null) {
                                System.out.println("Chunk doesn't exist.");
                                return;
                            }
                            CompoundTag tagOld = NbtIo.read(regionChunkInputStream);
                            regionChunkInputStream.close();
                            if(cx==ci && cz==cj){
                                ListTag<CompoundTag> sections = (ListTag<CompoundTag>) tagOld.getCompound("Level").getList("Sections");
                                for (int i = 0; i < sections.size() - 1; i++) {
                                    CompoundTag section = sections.get(i);
                                    int height = section.getByte("Y");
                                    if (height == y / 16) {
                                        setBlockInSectionChunk(block, section, x, y, z);
                                    }
                                }
                                CompoundTag finalSection = sections.get(sections.size() - 1);
                                sections.remove(sections.size() - 1);
                                for (int i = sections.size() - 1; i < y / 16 + 1; i++) {
                                    CompoundTag section = new CompoundTag();
                                    ListTag<CompoundTag> palette = (ListTag<CompoundTag>) section.getList("Palette");
                                    CompoundTag air = new CompoundTag();
                                    air.putString("Name", "minecraft:air");
                                    palette.add(air);
                                    long[] blockStates = new long[16 * 16 * 16 * 4 / 64];
                                    for (int j = 0; j < blockStates.length; j++) {
                                        blockStates[j] = 0;
                                    }
                                    section.put("Palette", palette);
                                    section.putByte("Y", (byte) i);
                                    section.putLongArray("BlockStates", blockStates);
                                    sections.add(section);
                                    if (i == y / 16) {
                                        setBlockInSectionChunk(block, section, x, y, z);
                                    }
                                }
                                finalSection.putByte("Y", (byte) (y / 16 + 1));
                                sections.add(finalSection);
                            }
                            DataOutputStream chunkDataOutputStream = nRF.getChunkDataOutputStream(ci, cj);
                            NbtIo.write(tagOld, chunkDataOutputStream);
                            chunkDataOutputStream.close();
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //RegionFile nRF= new RegionFile(new File(file+".mod"));
    }
    private static void readFile(RegionFile rF){
        try {
            for (int i=0; i<32; i++){
                for (int j=0; j<32; j++){
                    if(rF.hasChunk(i, j)){
                        DataInputStream regionChunkInputStream = rF.getChunkDataInputStream(i, j);
                        if(regionChunkInputStream==null){
                            continue;
                        }
                        try{
                            CompoundTag tagOld=NbtIo.read(regionChunkInputStream);
                            regionChunkInputStream.close();
                            readLevel(tagOld,0);
                            /*
                            DataOutputStream chunkDataOutputStream = nRF.getChunkDataOutputStream(i, j);
                            NbtIo.write(tagOld, chunkDataOutputStream);
                            chunkDataOutputStream.close();
                            */
                        }catch(NullPointerException NPE){
                            //System.out.print("NullPointerException.");
                            NPE.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                pW.close();
                fW.close();
            } catch (IOException ex) {
                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
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
                    if("Blocks".equals(tag.getName())){
                        for (int j = 0; j < 16; j++) {
                            pW.println("Y=" + j);
                            for (int k = 0; k < 16; k++) {
                                for (int i = 0; i < 16; i++) {
                                    pW.print(byteArrayTag.data[((j << 8) | (k << 4) | i)] + ", ");
                                }
                                pW.println();
                            }
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
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
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
    private static CompoundTag setLevel(CompoundTag oldTag){
        CompoundTag tag=new CompoundTag();
        tag.putInt("xPos", oldTag.getInt("xPos"));
        tag.putInt("zPos", oldTag.getInt("zPos"));
        tag.putLong("LastUpdate", oldTag.getLong("LastUpdate"));
        tag.putIntArray("HeightMap", oldTag.getIntArray("HeightMap"));
        //tag.putBoolean("TerrainPopulated", oldTag.getBoolean("TerrainPopulated"));

        ListTag<CompoundTag> sectionTags = new ListTag<CompoundTag>("Sections");
        ListTag<CompoundTag> sectionTagsOld =  (ListTag<CompoundTag>) oldTag.getList("Sections");
        for (int yBase = 0; yBase < sectionTagsOld.size(); yBase++) {
            byte[] blocks = oldTag.getByteArray("Blocks");
            byte yPos=oldTag.getByte("Y");
            CompoundTag section = new CompoundTag();
            //section.putByte("Y", yPos);
            section.putByteArray("Blocks", blocks);
            section.putByteArray("Data", oldTag.getByteArray("Data"));
            section.putByteArray("SkyLight", oldTag.getByteArray("SkyLight"));
            section.putByteArray("BlockLight", oldTag.getByteArray("BlockLight"));
            sectionTags.add(section);
        }
        tag.put("Sections", sectionTags);
        int[] biomes = oldTag.getIntArray("Biomes");
        if(biomes!=null){
            tag.putIntArray("Biomes", biomes);
        }
        tag.put("Entities", (ListTag<Tag>) oldTag.getList("Entities"));
        tag.put("TileEntities", (ListTag<Tag>) oldTag.getList("TileEntities"));
        tag.put("TileTicks", (ListTag<Tag>) oldTag.getList("TileTicks"));
        return tag;
    }
}
