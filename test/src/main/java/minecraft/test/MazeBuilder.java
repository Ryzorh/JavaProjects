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
import java.io.IOException;
import net.minecraft.world.level.chunk.storage.RegionFile;

/**
 *
 * @author eduard
 */
public class MazeBuilder {
    public void openChunkFiles(File fileInput, String[][] maze) throws IOException {
        if (fileInput.exists()) {
            File fileOutput = new File(fileInput.getPath() + ".mod");
            RegionFile rF = new RegionFile(fileInput);
            RegionFile nRF = new RegionFile(fileOutput);
            for(int ci=0; ci<32; ci++){
                for(int cj=0; cj<32; cj++){
                    if (rF.hasChunk(ci, cj)) {
                        DataInputStream regionChunkInputStream = rF.getChunkDataInputStream(ci, cj);
                        if (regionChunkInputStream == null) {
                            regionChunkInputStream.close();
                            return;
                        }
                        CompoundTag tagOld = NbtIo.read(regionChunkInputStream);
                        regionChunkInputStream.close();
                        String[][] chunkMaze=new String[16][16];
                        for(int i=0; i<chunkMaze.length; i++){
                            for(int j=0; j<chunkMaze[i].length; j++){
                                chunkMaze[i][j]=maze[i+ci*16][j+cj*16];
                            }
                        }
                        setBlocksInChunk(tagOld, chunkMaze);

                        DataOutputStream chunkDataOutputStream = nRF.getChunkDataOutputStream(ci, cj);
                        NbtIo.write(tagOld, chunkDataOutputStream);
                        chunkDataOutputStream.close();
                    }
                }
            }
        }
    }
    private void setUpSectionChunk(CompoundTag section, byte y, String[][] maze){
        ListTag<CompoundTag> palette = new ListTag<>();
        CompoundTag air = new CompoundTag();
        air.putString("Name", "minecraft:air");
        palette.add(air);
        CompoundTag bedRock = new CompoundTag();
        bedRock.putString("Name", "minecraft:bedrock");
        palette.add(bedRock);
        CompoundTag dirt = new CompoundTag();
        dirt.putString("Name", "minecraft:dirt");
        palette.add(dirt);
        CompoundTag grass = new CompoundTag();
        grass.putString("Name", "minecraft:grass_block");
        palette.add(grass);
        long[] blockStates = new long[16 * 16 * 16 * 4 / 64];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    long wall=y*16+i<80?1:0;
                    long nWll=y*16+i==0?1:y*16+i<60?2:y*16+i==60?3:0;
                    blockStates[j+i*16] = blockStates[j+i*16] | ("W".equals(maze[k][j])?(wall<<(4*(k+16*j))):(nWll<<(4*(k+16*j))));
                }
            }
        }
        section.put("Palette", palette);
        section.putByte("Y", y);
        section.putLongArray("BlockStates", blockStates);
    }
    
    private void setBlocksInChunk(CompoundTag tagOld, String[][] maze) {
        int height=-1;
        ListTag<CompoundTag> sections = (ListTag<CompoundTag>) tagOld.getCompound("Level").getList("Sections");
        for (int i = 0; i < sections.size(); i++) {
            CompoundTag section = sections.get(i);
            height = section.getByte("Y");
            if(height!=-1){
                sections.remove(i);
            }
        }
        /*
        for (int i = 0; i < sections.size(); i++) {
            CompoundTag section = sections.get(i);
            height = section.getByte("Y");
            if(height<=80/16 && height!=-1){
                setUpSectionChunk(section, (byte) height, maze);
            }else if (height!=-1){
                sections.remove(i);
            }
        }
        */
        for (int i = 0; i < 80 / 16 + 1; i++) {
            CompoundTag section = new CompoundTag();
            setUpSectionChunk(section, (byte) i, maze);
            sections.add(section);
        }
    }
}
