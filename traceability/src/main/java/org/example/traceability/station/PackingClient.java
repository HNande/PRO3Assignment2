package org.example.traceability.station;

import org.example.traceability.model.ProductRecord;
import java.util.List;

// Station 3 (packing). Throws StationUnavailableException if it can't be reached.
public interface PackingClient {

    // the trays the parts of the product came from
    List<Integer> getTrayIdsInProduct(int productId);

    // all products made with parts from one of the trays
    List<ProductRecord> getProductsForTrays(List<Integer> trayIds);

}
