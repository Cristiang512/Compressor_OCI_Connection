package com.zonafranca.compressor_services.dao;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zonafranca.compressor_services.entidades.TokenManage;

@Repository
public interface TokenManageDao extends JpaRepository<TokenManage, Integer>{
    
    @Query(value = "SELECT tm FROM TokenManage tm WHERE tm.process = :process")
    List<TokenManage> getExistingToken(@Param("process") String process);

    @Modifying
    @Query(value = "INSERT INTO TZFW_GESTION_TOKENS (CDPROCESO, CBTOKEN, CDUSERNAME, DTEXPIRACION) VALUES " +
                    "(:process, :token, :username, :expiration)", nativeQuery = true)
    @Transactional
    void insertNewToken(@Param("process") String process,
                            @Param("token") String token,
                            @Param("username") String username,
                            @Param("expiration") Date expiration);

    @Modifying
    @Query(value = "UPDATE TZFW_GESTION_TOKENS SET CBTOKEN =:token, DTEXPIRACION =:expiration WHERE CDPROCESO =:process", nativeQuery = true)
    @Transactional
    void updateExistingToken(@Param("token") String token,
                                  @Param("process") String process, 
                                  @Param("expiration") Date expiration);

}
