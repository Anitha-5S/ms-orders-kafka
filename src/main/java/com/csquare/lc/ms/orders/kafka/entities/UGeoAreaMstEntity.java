package com.csquare.lc.ms.orders.kafka.entities;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "u_geo_area_mst")
public class UGeoAreaMstEntity {
    private String cCode;
    private String cName;
    private String cShName;
    private String cGeoCityCode;
    private String cGeoLat;
    private String cGeoLon;
    private BigInteger nAudited;
    private BigInteger nPredefined;
    private String cCreateuser;
    private Timestamp dAdate;
    private Timestamp dLdate;
    private Timestamp tLtime;
    private String cModiuser;

    @Id
    @Column(name = "c_code", nullable = false, length = 6)
    public String getcCode() {
        return cCode;
    }

    public void setcCode(String cCode) {
        this.cCode = cCode;
    }

    @Basic
    @Column(name = "c_name", nullable = false, length = 40)
    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    @Basic
    @Column(name = "c_sh_name", nullable = false, length = 6)
    public String getcShName() {
        return cShName;
    }

    public void setcShName(String cShName) {
        this.cShName = cShName;
    }

    @Basic
    @Column(name = "c_geo_city_code", nullable = false, length = 6)
    public String getcGeoCityCode() {
        return cGeoCityCode;
    }

    public void setcGeoCityCode(String cGeoCityCode) {
        this.cGeoCityCode = cGeoCityCode;
    }

    @Basic
    @Column(name = "c_geo_lat", nullable = true, length = 12)
    public String getcGeoLat() {
        return cGeoLat;
    }

    public void setcGeoLat(String cGeoLat) {
        this.cGeoLat = cGeoLat;
    }

    @Basic
    @Column(name = "c_geo_lon", nullable = true, length = 12)
    public String getcGeoLon() {
        return cGeoLon;
    }

    public void setcGeoLon(String cGeoLon) {
        this.cGeoLon = cGeoLon;
    }

    @Basic
    @Column(name = "n_audited", nullable = false, precision = 0)
    public BigInteger getnAudited() {
        return nAudited;
    }

    public void setnAudited(BigInteger nAudited) {
        this.nAudited = nAudited;
    }

    @Basic
    @Column(name = "n_predefined", nullable = false, precision = 0)
    public BigInteger getnPredefined() {
        return nPredefined;
    }

    public void setnPredefined(BigInteger nPredefined) {
        this.nPredefined = nPredefined;
    }

    @Basic
    @Column(name = "c_createuser", nullable = false, length = 10)
    public String getcCreateuser() {
        return cCreateuser;
    }

    public void setcCreateuser(String cCreateuser) {
        this.cCreateuser = cCreateuser;
    }

    @Basic
    @Column(name = "d_adate", nullable = false)
    public Timestamp getdAdate() {
        return dAdate;
    }

    public void setdAdate(Timestamp dAdate) {
        this.dAdate = dAdate;
    }

    @Basic
    @Column(name = "d_ldate", nullable = false)
    public Timestamp getdLdate() {
        return dLdate;
    }

    public void setdLdate(Timestamp dLdate) {
        this.dLdate = dLdate;
    }

    @Basic
    @Column(name = "t_ltime", nullable = true)
    public Timestamp gettLtime() {
        return tLtime;
    }

    public void settLtime(Timestamp tLtime) {
        this.tLtime = tLtime;
    }

    @Basic
    @Column(name = "c_modiuser", nullable = true, length = 10)
    public String getcModiuser() {
        return cModiuser;
    }

    public void setcModiuser(String cModiuser) {
        this.cModiuser = cModiuser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UGeoAreaMstEntity that = (UGeoAreaMstEntity) o;
        return Objects.equals(cCode, that.cCode) &&
                Objects.equals(cName, that.cName) &&
                Objects.equals(cShName, that.cShName) &&
                Objects.equals(cGeoCityCode, that.cGeoCityCode) &&
                Objects.equals(cGeoLat, that.cGeoLat) &&
                Objects.equals(cGeoLon, that.cGeoLon) &&
                Objects.equals(nAudited, that.nAudited) &&
                Objects.equals(nPredefined, that.nPredefined) &&
                Objects.equals(cCreateuser, that.cCreateuser) &&
                Objects.equals(dAdate, that.dAdate) &&
                Objects.equals(dLdate, that.dLdate) &&
                Objects.equals(tLtime, that.tLtime) &&
                Objects.equals(cModiuser, that.cModiuser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cCode, cName, cShName, cGeoCityCode, cGeoLat, cGeoLon, nAudited, nPredefined, cCreateuser, dAdate, dLdate, tLtime, cModiuser);
    }
}
