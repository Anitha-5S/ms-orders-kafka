package com.csquare.lc.ms.orders.kafka.repos;

import com.csquare.lc.ms.orders.kafka.entities.UGeoAreaMstEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<UGeoAreaMstEntity, String> {

    @Query(value = "SELECT * FROM u_geo_area_mst ugam " +
                   " INNER JOIN  u_geo_city_mst ugcm ON ugam.c_geo_city_code = ugcm .c_code " +
                   " WHERE ugcm.c_code = :cityCode"
            , nativeQuery = true)
    List<UGeoAreaMstEntity> findByCityCode(@Param("cityCode") String cityCode);

    @Query(value = "SELECT * FROM u_geo_area_mst ugam " +
                  " INNER JOIN  u_geo_city_mst ugcm ON ugam.c_geo_city_code = ugcm .c_code " +
                  " INNER JOIN  u_geo_district_mst ugdm ON ugdm.c_code = ugcm.c_geo_district_code " +
                  " INNER JOIN  u_geo_state_mst ugsm ON ugsm.c_code  = ugdm.c_geo_state_code " +
                  " WHERE ugsm.c_code = :stateCode"
            , nativeQuery = true)
    List<UGeoAreaMstEntity> findByStateCode(@Param("stateCode") String stateCode);
}
