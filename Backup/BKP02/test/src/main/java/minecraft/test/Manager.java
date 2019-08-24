/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minecraft.test;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.NbtIo;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.world.level.chunk.storage.RegionFile;

/**
 *
 * @author Eduard
 */
public class Manager {
    private Map<Coordinate, Block> bList=null;
    public Manager(){
        bList= new HashMap<>();
    }
    public void putBlock(Coordinate coord, Block block){
        bList.put(coord, block);
    }
    public Block getBlock(Coordinate coord){
        return bList.get(coord);
    }
    private void addRegionFiles(File baseFolder, ArrayList<File> regionFiles) {

        File regionFolder = new File(baseFolder, "region");
        File[] list = regionFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(RegionFile.ANVIL_EXTENSION);
            }
        });

        if (list != null) {
            for (File file : list) {
                regionFiles.add(file);
            }
        }
    }
    private void setUpSectionChunk(CompoundTag section, byte i){
        ListTag<CompoundTag> palette = (ListTag<CompoundTag>) section.getList("Palette");
        CompoundTag air = new CompoundTag();
        air.putString("Name", "minecraft:air");
        palette.add(air);
        long[] blockStates = new long[16 * 16 * 16 * 4 / 64];
        for (int j = 0; j < blockStates.length; j++) {
            blockStates[j] = 0;
        }
        section.put("Palette", palette);
        section.putByte("Y", i);
        section.putLongArray("BlockStates", blockStates);
    }
    private void setBlockInChunk(CompoundTag tagOld, Block block, int x, int y, int z) {
        ListTag<CompoundTag> sections = (ListTag<CompoundTag>) tagOld.getCompound("Level").getList("Sections");
        for (int i = 0; i < sections.size() - 1; i++) {
            CompoundTag section = sections.get(i);
            int height = section.getByte("Y");
            if(section.getLongArray("BlockStates").length==0){
                setUpSectionChunk(section,(byte)height);
            }
            if (height == y / 16) {
                setBlockInSectionChunk(block, section, x, y, z);
            }
        }
        CompoundTag finalSection = sections.get(sections.size() - 1);
        sections.remove(sections.size() - 1);
        for (int i = sections.size() - 2; i < y / 16 + 1; i++) {
            CompoundTag section = new CompoundTag();
            setUpSectionChunk(section,(byte)i);
            sections.add(section);
            if (i == y / 16) {
                setBlockInSectionChunk(block, section, x, y, z);
            }
        }
        if(sections.size()<17){
            finalSection.putByte("Y", (byte) (sections.size()-1));
            sections.add(finalSection);
        }
    }
    private long[] convertArray(long[] data, int aLen, int x, int y, int z, int id){
        long[] conv=new long[16*16*16*aLen/64];
        for(int i=0; i<conv.length; i++){
            conv[i]=0;
        }
        int bLen=data.length*64/(16*16*16);
        if(data.length*64%(16*16*16)!=0 ){
            System.out.println("BlockStates length array is not a multiple of the number of blocks.");
            return data;
        }
        int b111=getB111(bLen);
        int lLen = 64;
        int line01 = 0;
        int pos01 = 0;
        int line02 = 0;
        int pos02 = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    int cLen = lLen - pos01;
                    int c111 = getB111(cLen);
                    int dLen = bLen - cLen;
                    int d111 = getB111(dLen);
                    long block = pos01 + bLen <= lLen ? (data[line01] >> pos01) & b111 : ((data[line01] >> pos01) & c111) | ((data[line01 + 1] & d111) << cLen);
                    line01 = pos01 + bLen < lLen ? line01 : line01 + 1;
                    pos01 = pos01 + bLen < lLen ? pos01 + bLen : bLen - (lLen - pos01);
                    
                    block=x==k && z==j && i==y? id:block;
                    
                    int eLen = lLen - pos02;
                    int e111 = getB111(eLen);
                    int fLen = aLen - eLen;
                    int f111 = getB111(fLen);
                    conv[line02]=pos02 + aLen <= lLen ? conv[line02]|(block<<pos02):conv[line02]|((block&e111)<<pos02);
                    if(pos02 + aLen > lLen){
                        conv[line02+1]=pos02 + aLen <= lLen ? conv[line02+1]:conv[line02+1]|((block>>eLen)&f111);
                    }
                    line02 = pos02 + aLen < lLen ? line02 : line02 + 1;
                    pos02 = pos02 + aLen < lLen ? pos02 + aLen : aLen - (lLen - pos02);
                }
            }
        }
        return conv;
    }
    private void setBlockInSectionChunk(Block block, CompoundTag section, int x, int y, int z) {
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
        for (int k = 4; k < 32; k++) {
            if (Math.pow(2, k) >= palette.size()) {
                bLen = k;
                break;
            }
        }
        long[] blockStates = section.getLongArray("BlockStates");
        blockStates=convertArray(blockStates, bLen, xInChunk, yInChunk, zInChunk, posInPalette);
        section.putLongArray("BlockStates", blockStates);/*
        long mold = ((long) b111 << posInArray % 64);
        long data = blockStates[posInArray / 64];
        long newData = (data & ~mold) | ((long)posInPalette << posInArray % 64);
        blockStates[posInArray / 64] = newData;*/
    }
    private static int getB111(int bLen) {
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
    public void save(String folder){
        String rFolder=folder;
        ArrayList <File> regionFiles=new ArrayList<>();
        addRegionFiles(new File(rFolder),regionFiles);
        for(int s=0; s<regionFiles.size(); s++){
            File fileInput = regionFiles.get(s);
            File fileOutput = new File(rFolder+"/region/"+regionFiles.get(s).getName()+".mod");
            if (fileInput.exists()) {
                RegionFile rF = new RegionFile(fileInput);
                RegionFile nRF = new RegionFile(fileOutput);
                int rx=Integer.parseInt(fileInput.getName().split("\\.")[1]);
                int rz=Integer.parseInt(fileInput.getName().split("\\.")[2]);
                try {
                    for (int ci = 0; ci < 32; ci++) {
                        for (int cj = 0; cj < 32; cj++) {
                            if (rF.hasChunk(ci, cj)) {
                                DataInputStream regionChunkInputStream = rF.getChunkDataInputStream(ci, cj);
                                if (regionChunkInputStream == null) {
                                    System.out.println("Chunk doesn't exist.");
                                    return;
                                }
                                CompoundTag tagOld = NbtIo.read(regionChunkInputStream);
                                regionChunkInputStream.close();
                                for (int y=0; y<256;y++){
                                    for (int z=0; z<16;z++){
                                        for (int x=0; x<16;x++){
                                            Block block=bList.get(new Coordinate(rx*32*16+ci*16+x, y, rz*32*16+cj*16+z));
                                            if(block!=null){
                                                setBlockInChunk(tagOld, block, rx*32*16+ci*16+x, y, rz*32*16+cj*16+z);
                                            }
                                        }
                                    }
                                }
                                DataOutputStream chunkDataOutputStream = nRF.getChunkDataOutputStream(ci, cj);
                                NbtIo.write(tagOld, chunkDataOutputStream);
                                chunkDataOutputStream.close();
                            }
                        }
                    }
                    rF.close();
                    nRF.close();
                } catch (IOException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            new File(fileInput.getParent()+"/backup").mkdirs();
            fileInput.renameTo(new File(fileInput.getParent()+"/backup/"+fileInput.getName()));
            System.out.println(fileInput.getParent()+"/backup/"+fileInput.getName());
            fileOutput.renameTo(new File(fileInput.getPath()));
        }
    }
    public int getHeighestSection(String folder, int x, int z){
        File fileInput = new File(folder+"/region/"+"r."+((x/16)/32)+"."+((z/16)/32)+".mca");
        if (fileInput.exists()) {
            RegionFile rF = new RegionFile(fileInput);
            if (rF.hasChunk((x / 16) % 32, (z / 16) % 32)) {
                try {
                    DataInputStream regionChunkInputStream = rF.getChunkDataInputStream((x / 16) % 32, (z / 16) % 32);
                    if (regionChunkInputStream == null) {
                        System.out.println("Chunk doesn't exist.");
                        return 0;
                    }
                    CompoundTag tagOld = NbtIo.read(regionChunkInputStream);
                    regionChunkInputStream.close();
                    ListTag<CompoundTag> sections=(ListTag<CompoundTag>)tagOld.getCompound("Level").getList("Sections");
                    int result=sections.size()-2;
                    rF.close();
                    return result;
                } catch (IOException ex) {
                    Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return 0;
    }
}
