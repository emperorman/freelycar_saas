package com.freelycar.saas.project.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author tangwei - Toby
 * @date 2018/10/17
 * @email toby911115@gmail.com
 */
@Entity
@Table
@DynamicInsert
@DynamicUpdate
public class Client implements Serializable {
    private static final long serialVersionUID = 4L;

    /**
     * 主键ID
     */
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @NotNull
    @Length(max = 50)
    private String id;

    /**
     * 删除标记位（0：有效；1：无效）
     */
    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean delStatus;

    /**
     * 创建时间
     */
    @Column(nullable = false, columnDefinition = "datetime default NOW()")
    private Timestamp createTime;

    /**
     * 年龄
     */
    @Column(length = 3)
    private Integer age;

    /**
     * 生日
     */
    @Column
    private Date birthday;

    /**
     * 消费总额
     */
    @Column
    private Double consumeAmount;

    /**
     * 消费次数
     */
    @Column
    private Integer consumeTimes;

    /**
     * 性别
     */
    @Column
    private String gender;

    /**
     * 身份证号
     */
    @Column
    private String idNumber;

    /**
     * 行驶证号
     */
    @Column
    private String driverLicense;

    /**
     * 是否会员（0：不是会员；1：是会员）
     */
    @Column(nullable = false)
    private Boolean isMember;

    /**
     * 开通会员的时间
     */
    @Column
    private Timestamp memberDate;

    /**
     * 最后登录时间
     */
    @Column
    private Timestamp lastVisit;

    /**
     * 姓名
     */
    @Column
    private String name;

    /**
     * 手机号码
     */
    @Column
    private String phone;

    /**
     *
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer points;


    @Column
    private String recommendName;

    @Column(length = 3, nullable = false, columnDefinition = "int default 0")
    private Integer state;

    /**
     * 真实姓名（智能柜用）
     */
    @Column
    private String trueName;

    /**
     * 昵称（从微信获取来）
     */
    @Column
    private String nickName;

    /**
     * 所属门店
     */
    @Column
    private String storeId;

    public Client() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getDelStatus() {
        return delStatus;
    }

    public void setDelStatus(Boolean delStatus) {
        this.delStatus = delStatus;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Double getConsumeAmount() {
        return consumeAmount;
    }

    public void setConsumeAmount(Double consumeAmount) {
        this.consumeAmount = consumeAmount;
    }

    public Integer getConsumeTimes() {
        return consumeTimes;
    }

    public void setConsumeTimes(Integer consumeTimes) {
        this.consumeTimes = consumeTimes;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public Boolean getMember() {
        return isMember;
    }

    public void setMember(Boolean member) {
        isMember = member;
    }

    public Timestamp getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(Timestamp lastVisit) {
        this.lastVisit = lastVisit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
    }

    public Timestamp getMemberDate() {
        return memberDate;
    }

    public void setMemberDate(Timestamp memberDate) {
        this.memberDate = memberDate;
    }

    public String getRecommendName() {
        return recommendName;
    }

    public void setRecommendName(String recommendName) {
        this.recommendName = recommendName;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getTrueName() {
        return trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":\"")
                .append(id).append('\"');
        sb.append(",\"delStatus\":")
                .append(delStatus);
        sb.append(",\"createTime\":\"")
                .append(createTime).append('\"');
        sb.append(",\"age\":")
                .append(age);
        sb.append(",\"birthday\":\"")
                .append(birthday).append('\"');
        sb.append(",\"consumeAmount\":")
                .append(consumeAmount);
        sb.append(",\"consumeTimes\":")
                .append(consumeTimes);
        sb.append(",\"gender\":\"")
                .append(gender).append('\"');
        sb.append(",\"idNumber\":\"")
                .append(idNumber).append('\"');
        sb.append(",\"driverLicense\":\"")
                .append(driverLicense).append('\"');
        sb.append(",\"isMember\":")
                .append(isMember);
        sb.append(",\"memberDate\":\"")
                .append(memberDate).append('\"');
        sb.append(",\"lastVisit\":\"")
                .append(lastVisit).append('\"');
        sb.append(",\"name\":\"")
                .append(name).append('\"');
        sb.append(",\"phone\":\"")
                .append(phone).append('\"');
        sb.append(",\"points\":")
                .append(points);
        sb.append(",\"recommendName\":\"")
                .append(recommendName).append('\"');
        sb.append(",\"state\":")
                .append(state);
        sb.append(",\"trueName\":\"")
                .append(trueName).append('\"');
        sb.append(",\"nickName\":\"")
                .append(nickName).append('\"');
        sb.append(",\"storeId\":\"")
                .append(storeId).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
