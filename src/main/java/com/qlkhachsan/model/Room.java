package com.qlkhachsan.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Phong cua khach san - bang {@code rooms_by_hotel} (Q2).
 *
 * <pre>
 * PRIMARY KEY (hotel_id, room_number)
 * </pre>
 */
@Table("rooms_by_hotel")
public class Room {

    @PrimaryKey
    private RoomKey key = new RoomKey();

    @Column("room_type")
    private String roomType;

    @Column("price_per_night")
    private BigDecimal pricePerNight;

    @Column("status")
    private RoomStatus status = RoomStatus.AVAILABLE;

    // ===== BO SUNG =====
    @Column("capacity")
    private Integer capacity;

    @Column("floor")
    private Integer floor;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    /** Ten khach san - chi dung de hien thi, khong luu vao bang rooms_by_hotel. */
    @Transient
    private String hotelName;

    public Room() {
    }

    public Room(String hotelId, Integer roomNumber) {
        this.key = new RoomKey(hotelId, roomNumber);
    }

    /* ================== TIEN ICH ================== */

    @Transient
    public String getHotelId() {
        return key == null ? null : key.getHotelId();
    }

    @Transient
    public Integer getRoomNumber() {
        return key == null ? null : key.getRoomNumber();
    }

    /**
     * Setter tien ich cho Spring MVC binding (form them/sua phong).
     * Ghi vao khoa chinh {@link RoomKey}, khong la cot rieng cua bang.
     */
    @Transient
    public void setHotelId(String hotelId) {
        if (key == null) {
            key = new RoomKey();
        }
        key.setHotelId(hotelId);
    }

    @Transient
    public void setRoomNumber(Integer roomNumber) {
        if (key == null) {
            key = new RoomKey();
        }
        key.setRoomNumber(roomNumber);
    }

    @Transient
    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }

    /** Ma phong hien thi, vi du "KS001-101". */
    @Transient
    public String getDisplayCode() {
        return getHotelId() + "-" + getRoomNumber();
    }

    /* ================== GETTER / SETTER ================== */

    public RoomKey getKey() {
        return key;
    }

    public void setKey(RoomKey key) {
        this.key = key;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    @Override
    public String toString() {
        return "Room{key=" + key + ", roomType=" + roomType + ", status=" + status + "}";
    }
}