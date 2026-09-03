package com.xome.auction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auction")
@CrossOrigin(origins = "http://localhost:3000")
public class AuctionController {

    private static final Logger log = LoggerFactory.getLogger(AuctionController.class);

    private final AuctionStore store;

    public AuctionController(AuctionStore store) {
        this.store = store;
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }

    @PostMapping("/bids")
    public ResponseEntity<Map<String, Object>> placeBid(@RequestBody BidRequest request) {
        log.info("received bid request for bidder={} amount={} requestId={}", request.bidderId(), request.amount(), request.requestId());

        Map<String, Object> status = currentStatus();

        if (request == null || request.bidderId() == null || request.bidderId().isBlank() || request.requestId() == null || request.requestId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "accepted", false,
                "reason", "bidderId and requestId are required",
                "status", status));
        }

        if (request.amount() <= 0L) {
            return ResponseEntity.badRequest().body(Map.of(
                "accepted", false,
                "reason", "amount must be positive",
                "status", status));
        }

        if (store.containsRequestId(request.requestId())) {
            log.info("duplicate bid request ignored: {}", request.requestId());
            return ResponseEntity.ok(Map.of(
                "accepted", true,
                "reason", "duplicate request",
                "status", status));
        }

        Map<String, Long> maxima = store.highestBidByBidder();
        long currentLeaderMax = maxima.values().stream().mapToLong(Long::longValue).max().orElse(AuctionStore.STARTING_PRICE);

        if (!maxima.isEmpty() && request.amount() <= currentLeaderMax) {
            log.info("bid rejected: amount={} is not above current leader max={}", request.amount(), currentLeaderMax);
            return ResponseEntity.ok(Map.of(
                "accepted", false,
                "reason", "Below the current highest maximum",
                "status", status));
        }

        Bid bid = new Bid(request.requestId(), request.bidderId(), request.amount(), Instant.now());
        store.addBid(bid);

        log.info("bid accepted: bidder={} amount={} requestId={}", request.bidderId(), request.amount(), request.requestId());
        return ResponseEntity.ok(Map.of(
            "accepted", true,
            "reason", "Accepted",
            "status", currentStatus()));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(currentStatus());
    }

    private Map<String, Object> currentStatus() {
        Map<String, Long> maxima = store.highestBidByBidder();

        Map<String, Object> status = new HashMap<>();
        if (maxima.isEmpty()) {
            status.put("currentPrice", AuctionStore.STARTING_PRICE);
            status.put("leadingBidder", null);
            status.put("reserveMet", false);
            status.put("closesAt", store.closesAt().toString());
            return status;
        }

        List<Map.Entry<String, Long>> sorted = new ArrayList<>(maxima.entrySet());
        sorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        long highest = sorted.get(0).getValue();
        String leadingBidder = sorted.get(0).getKey();
        long secondHighest = sorted.size() > 1 ? sorted.get(1).getValue() : AuctionStore.STARTING_PRICE;

        long currentPrice = sorted.size() > 1 ? Math.max(AuctionStore.STARTING_PRICE, secondHighest + AuctionStore.INCREMENT) : AuctionStore.STARTING_PRICE;
        boolean reserveMet = currentPrice >= store.reservePrice();

        status.put("currentPrice", currentPrice);
        status.put("leadingBidder", leadingBidder);
        status.put("reserveMet", reserveMet);
        status.put("closesAt", store.closesAt().toString());
        return status;
    }
}
