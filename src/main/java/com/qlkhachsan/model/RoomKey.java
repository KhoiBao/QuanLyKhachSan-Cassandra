package com.qlkhachsan.model;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

/**
 * Khoa chinh cua bang {@code rooms_by_hotel}.
 *
 * <pre>
 * PRIMARY KEY (hotel_id, room_number)
 * </pre>
 */
@PrimaryKeyClass
public class RoomKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(name = "hotel_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String hotelId;

    @PrimaryKeyColumn(name = "room_number", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Integer roomNumber;

    public RoomKey() {
    }

    public RoomKey(String hotelId, Integer roomNumber) {
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoomKey other)) {
            return false;
        }
        return Objects.equals(hotelId, other.hotelId) && Objects.equals(roomNumber, other.roomNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotelId, roomNumber);
    }

    @Override
    public String toString() {
        return "RoomKey{hotelId=" + hotelId + ", roomNumber=" + roomNumber + "}";
    }
}