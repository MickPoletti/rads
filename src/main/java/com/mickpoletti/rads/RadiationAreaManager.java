package com.mickpoletti.rads;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openjdk.nashorn.internal.parser.JSONParser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.ibm.icu.impl.UResource.Array;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.channel.FileRegion;
import net.minecraft.SystemReport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class RadiationAreaManager {
    public static final Codec<RadiationArea> RADS_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("pX1").forGetter(RadiationArea::pX1),
            Codec.INT.fieldOf("pX2").forGetter(RadiationArea::pX2),
            Codec.INT.fieldOf("pY1").forGetter(RadiationArea::pY1),
            Codec.INT.fieldOf("pY2").forGetter(RadiationArea::pY2),
            Codec.INT.fieldOf("pZ1").forGetter(RadiationArea::pZ1),
            Codec.INT.fieldOf("pZ2").forGetter(RadiationArea::pZ2)
        ).apply(instance, RadiationArea::new)
    );
    public static final Codec<List<RadiationArea>> RADS_LIST_CODEC = RADS_CODEC.listOf();

    public void save(List<RadiationArea> radiationAreas, String saveDirectory) {
        File file = new File(saveDirectory);
        try (FileWriter fos = new FileWriter(file)) {
            RADS_LIST_CODEC.encodeStart(JsonOps.INSTANCE, radiationAreas)
                .resultOrPartial(e -> System.err.println(e))
                .ifPresent(decodedObj -> {
                    try {
                        fos.write(decodedObj.toString());
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                });
            // fos.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    public List<RadiationArea> loadRadiationAreasFromFile(String filePath) {
        File file = new File(filePath);
        List<RadiationArea> radiationAreas = new ArrayList<>();
        if (!file.exists() || file.isDirectory()) {   
            return radiationAreas;
        }

        FileReader r;
        try {
            r = new FileReader(filePath);
            try (JsonReader reader = new JsonReader(r)) {
                JsonElement test = JsonParser.parseReader(reader);
    
                return RADS_LIST_CODEC.parse(JsonOps.INSTANCE, test)
                    .resultOrPartial(e -> System.out.println(e)).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return radiationAreas;
    }

}
