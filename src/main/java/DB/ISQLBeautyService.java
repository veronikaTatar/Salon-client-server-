package DB;

import Salon.BeautyService;

import java.sql.SQLException;
import java.util.List;

public interface ISQLBeautyService {
    boolean insert(BeautyService beautyService);

    boolean update(BeautyService beautyService);

    boolean deleteServiceRecord(BeautyService beautyService) throws SQLException;

    List<BeautyService> getAllBeautyServices() throws SQLException;

    boolean checkIfServiceExists(int serviceId);
}