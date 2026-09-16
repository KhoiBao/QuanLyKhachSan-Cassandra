package com.qlkhachsan.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.BookingStatus;
import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.Room;
import com.qlkhachsan.model.RoomKey;
import com.qlkhachsan.model.RoomStatus;
import com.qlkhachsan.repository.BookingStore;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.repository.RoomStore;

/**
 * Nghiep vu phong: loc theo khach san/loai/trang thai, them/sua/xoa phong,
 * luon dong bo lai so phong cua khach san (hotels.total_rooms).
 */
@Service
public class RoomService {

    /** Cac loai phong cho dropdown (khop voi du lieu mau). */
    public static final List<String> ROOM_TYPES = List.of("Standard", "Deluxe", "Suite");

    private final RoomStore roomStore;
    private final HotelStore hotelStore;
    private final BookingStore bookingStore;

    public RoomService(RoomStore roomStore, HotelStore hotelStore, BookingStore bookingStore) {
        this.roomStore = roomStore;
        this.hotelStore = hotelStore;
        this.bookingStore = bookingStore;
    }

    public List<Room> search(String hotelId, String roomType, RoomStatus status) {
        List<Room> result = new ArrayList<>();
        for (Room room : roomStore.findAll()) {
            if (hotelId != null && !hotelId.isBlank() && !hotelId.equals(room.getHotelId())) {
                continue;
            }
            if (roomType != null && !roomType.isBlank() && !roomType.equalsIgnoreCase(room.getRoomType())) {
                continue;
            }
            if (status != null && room.getStatus() != status) {
                continue;
            }
            result.add(room);
        }
        return decorate(result);
    }

    public List<Room> findByHotel(String hotelId) {
        return roomStore.findByHotel(hotelId);
    }

    public Room getById(String hotelId, Integer roomNumber) {
        Room room = roomStore.findById(new RoomKey(hotelId, roomNumber)).orElse(null);
        if (room == null) {
            throw new ValidationException("Không tìm thấy phòng " + hotelId + "-" + roomNumber);
        }
        return room;
    }

    public void create(Room form) {
        validate(form);
        Hotel hotel = hotelStore.findById(form.getHotelId())
                .orElseThrow(() -> new ValidationException("Khách sạn không tồn tại."));
        if (roomStore.findById(new RoomKey(hotel.getHotelId(), form.getRoomNumber())).isPresent()) {
            throw new ValidationException("Số phòng " + form.getRoomNumber() + " đã tồn tại trong khách sạn này.");
        }
        if (form.getCreatedAt() == null) {
            form.setCreatedAt(java.time.Instant.now());
        }
        roomStore.save(form);
        syncTotalRooms(hotel.getHotelId());
    }

    public void update(String hotelId, Integer roomNumber, Room form) {
        validate(form);
        Room room = getById(hotelId, roomNumber);
        // Doi so phong: phong moi phai chua ton tai trong cung khach san
        Integer newNumber = form.getRoomNumber();
        if (newNumber != null && !newNumber.equals(roomNumber)) {
            if (roomStore.findById(new RoomKey(hotelId, newNumber)).isPresent()) {
                throw new ValidationException("Số phòng " + newNumber + " đã tồn tại trong khách sạn này.");
            }
            roomStore.deleteById(room.getKey());
            room.setKey(new RoomKey(hotelId, newNumber));
        }
        room.setRoomType(form.getRoomType());
        room.setPricePerNight(form.getPricePerNight());
        room.setStatus(form.getStatus() == null ? room.getStatus() : form.getStatus());
        room.setCapacity(form.getCapacity());
        room.setFloor(form.getFloor());
        roomStore.save(room);
        syncTotalRooms(hotelId);
    }

    public void delete(String hotelId, Integer roomNumber) {
        Room room = getById(hotelId, roomNumber);
        for (BookingById booking : bookingStore.findAll()) {
            boolean sameRoom = hotelId.equals(booking.getHotelId())
                    && roomNumber.equals(booking.getRoomNumber());
            boolean active = booking.getStatus() == BookingStatus.PENDING
                    || booking.getStatus() == BookingStatus.CONFIRMED
                    || booking.getStatus() == BookingStatus.CHECKED_IN;
            if (sameRoom && active) {
                throw new ValidationException("Không thể xóa phòng đang có đơn đặt phòng chưa kết thúc.");
            }
        }
        roomStore.deleteById(room.getKey());
        syncTotalRooms(hotelId);
    }

    /** Dong bo lai so phong thuc te cua khach san. */
    private void syncTotalRooms(String hotelId) {
        hotelStore.findById(hotelId).ifPresent(hotel -> {
            hotel.setTotalRooms(roomStore.findByHotel(hotelId).size());
            hotelStore.save(hotel);
        });
    }

    private void validate(Room room) {
        if (room.getHotelId() == null || room.getHotelId().isBlank()) {
            throw new ValidationException("Hãy chọn khách sạn.");
        }
        if (room.getRoomNumber() == null || room.getRoomNumber() < 1) {
            throw new ValidationException("Số phòng phải lớn hơn 0.");
        }
        if (room.getRoomType() == null || room.getRoomType().isBlank()) {
            throw new ValidationException("Hãy chọn loại phòng.");
        }
        if (room.getPricePerNight() == null || room.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Giá phòng mỗi đêm phải lớn hơn 0.");
        }
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE);
        }
        if (room.getCapacity() == null || room.getCapacity() < 1) {
            room.setCapacity(2);
        }
        if (room.getFloor() == null || room.getFloor() < 1) {
            room.setFloor(room.getRoomNumber() / 100 + 1);
        }
    }

    /** Gan ten khach san vao tung phong de hien thi (cot Khách sạn). */
    private List<Room> decorate(List<Room> rooms) {
        Map<String, String> names = new LinkedHashMap<>();
        for (Room room : rooms) {
            String name = names.computeIfAbsent(room.getHotelId(), id ->
                    hotelStore.findById(id).map(Hotel::getHotelName).orElse(id));
            room.setHotelName(name);
        }
        return rooms;
    }
}
