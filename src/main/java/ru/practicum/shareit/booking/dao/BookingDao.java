package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookingDao extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId ORDER BY b.start DESC")
    Collection<Booking> findAllUserBookingByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND (b.start < :currentDate AND b.end > :currentDate) " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findCurrentUserBookingByUserId(@Param("userId") Long userId,
                                                       @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND  b.end < :currentDate " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findPastUserBookingByUserId(@Param("userId") Long userId,
                                                    @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND  b.start > :currentDate " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findFutureUserBookingByUserId(@Param("userId") Long userId,
                                                      @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND b.status = 'WAITING' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findWaitingUserBookingByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId " +
            "AND b.status = 'REJECTED' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findRejectedUserBookingByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds ORDER BY b.start DESC")
    Collection<Booking> findAllOwnerItemsBookingById(@Param("itemIds") Collection<Long> itemIds);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND (b.start < :currentDate AND b.end > :currentDate) " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findCurrentOwnerItemsBookingById(@Param("itemIds") Collection<Long> itemIds,
                                                         @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND  b.end < :currentDate " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findPastOwnerItemsBookingById(@Param("itemIds") Collection<Long> itemIds,
                                                      @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND  b.start > :currentDate " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findFutureOwnerItemsBookingById(@Param("itemIds") Collection<Long> itemIds,
                                                        @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.status = 'WAITING' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findWaitingOwnerItemsBookingById(@Param("itemIds") Collection<Long> itemIds);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.status = 'REJECTED' " +
            "ORDER BY b.start DESC")
    Collection<Booking> findRejectedOwnerItemsBookingById(@Param("itemIds") Collection<Long> itemIds);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :currentDate " +
            "ORDER BY b.end DESC")
    Optional<Booking> findLastBooking(@Param("itemId") Long itemId,
                                      @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :currentDate " +
            "ORDER BY b.start ASC")
    Optional<Booking> findNextBooking(@Param("itemId") Long itemId,
                                      @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId")
    Optional<Booking> findBookingByUserIdAndItemId(@Param("userId") Long userId,
                                                   @Param("itemId") Long itemId);
}
