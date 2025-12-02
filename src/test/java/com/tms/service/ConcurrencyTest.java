package com.tms.service;

import com.tms.dto.BookingRequest;
import com.tms.entity.*;
import com.tms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private TransporterRepository transporterRepository;

    @Autowired
    private TruckCapacityRepository truckCapacityRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Load testLoad;
    private Transporter transporter1;
    private Transporter transporter2;
    private Bid bid1;
    private Bid bid2;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        bidRepository.deleteAll();
        loadRepository.deleteAll();
        truckCapacityRepository.deleteAll();
        transporterRepository.deleteAll();

        testLoad = new Load();
        testLoad.setShipperId("SHIP001");
        testLoad.setLoadingCity("Mumbai");
        testLoad.setUnloadingCity("Delhi");
        testLoad.setLoadingDate(LocalDateTime.now().plusDays(5));
        testLoad.setProductType("Electronics");
        testLoad.setWeight(5000.0);
        testLoad.setWeightUnit("KG");
        testLoad.setTruckType("CONTAINER-20FT");
        testLoad.setNoOfTrucks(1);
        testLoad.setStatus("OPEN_FOR_BIDS");
        testLoad = loadRepository.save(testLoad);

        transporter1 = new Transporter();
        transporter1.setCompanyName("Transport A");
        transporter1.setRating(4.5);
        transporter1 = transporterRepository.save(transporter1);

        TruckCapacity tc1 = new TruckCapacity("CONTAINER-20FT", 5);
        tc1.setTransporter(transporter1);
        truckCapacityRepository.save(tc1);

        transporter2 = new Transporter();
        transporter2.setCompanyName("Transport B");
        transporter2.setRating(4.0);
        transporter2 = transporterRepository.save(transporter2);

        TruckCapacity tc2 = new TruckCapacity("CONTAINER-20FT", 5);
        tc2.setTransporter(transporter2);
        truckCapacityRepository.save(tc2);

        bid1 = new Bid();
        bid1.setLoadId(testLoad.getLoadId());
        bid1.setTransporterId(transporter1.getTransporterId());
        bid1.setProposedRate(50000.0);
        bid1.setTrucksOffered(1);
        bid1.setStatus("PENDING");
        bid1 = bidRepository.save(bid1);

        bid2 = new Bid();
        bid2.setLoadId(testLoad.getLoadId());
        bid2.setTransporterId(transporter2.getTransporterId());
        bid2.setProposedRate(48000.0);
        bid2.setTrucksOffered(1);
        bid2.setStatus("PENDING");
        bid2 = bidRepository.save(bid2);
    }

    @Test
    void concurrentBooking_OnlyOneSucceeds() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        BookingRequest request1 = new BookingRequest();
        request1.setBidId(bid1.getBidId());
        request1.setAllocatedTrucks(1);

        BookingRequest request2 = new BookingRequest();
        request2.setBidId(bid2.getBidId());
        request2.setAllocatedTrucks(1);

        executor.submit(() -> {
            try {
                bookingService.createBooking(request1);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                bookingService.createBooking(request2);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executor.shutdown();

        assertEquals(1, successCount.get(), "Only one booking should succeed");
        assertEquals(1, failureCount.get(), "One booking should fail");
    }

    @Test
    void optimisticLocking_VersionIncrementsOnUpdate() {
        Integer initialVersion = testLoad.getVersion();
        
        testLoad.setStatus("BOOKED");
        Load savedLoad = loadRepository.save(testLoad);
        
        assertNotNull(savedLoad.getVersion());
        assertTrue(savedLoad.getVersion() > initialVersion || initialVersion == null);
    }
}
