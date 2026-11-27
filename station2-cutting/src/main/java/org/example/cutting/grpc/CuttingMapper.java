package org.example.cutting.grpc;

import org.example.cutting.model.PartRecord;
import org.example.cutting.model.TrayRecord;
import org.example.slaughterhouse.grpc.Part;
import org.example.slaughterhouse.grpc.Tray;
import java.util.List;

public final class CuttingMapper {

    private CuttingMapper() {
    }

    public static Part toProto(PartRecord part) {
        return Part.newBuilder()
                .setId(part.id())
                .setPartType(part.partType())
                .setWeightKg(part.weightKg())
                .setAnimalRegNo(part.animalRegNo())
                .setTrayId(part.trayId())
                .build();
    }

    public static Tray toProto(TrayRecord tray, List<PartRecord> parts) {
        Tray.Builder builder = Tray.newBuilder()
                .setId(tray.id())
                .setPartType(tray.partType())
                .setMaxWeightKg(tray.maxWeightKg())
                .setCurrentWeightKg(tray.currentWeightKg())
                .setClosed(tray.closed());
        for (PartRecord part : parts) {
            builder.addParts(toProto(part));
        }
        return builder.build();
    }
}
