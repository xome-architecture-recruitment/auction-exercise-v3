package com.xome.auction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class AuctionStore {

    public static final long STARTING_PRICE = 250_000L;
    public static final long INCREMENT      = 5_000L;
    public static final long RESERVE_PRICE  = 300_000L;

    private final Instant closesAt = Instant.now().plusSeconds(3600);
    private final List<Bid> bids = new ArrayList<>();
    private final Map<String, Bid> highestBidByBidder = new HashMap<>();
    private final Set<String> requestIds = new HashSet<>();

    public Instant closesAt() {
        return closesAt;
    }

    public List<Bid> bids() {
        return bids;
    }

    public long reservePrice() {
        return RESERVE_PRICE;
    }

    public boolean containsRequestId(String requestId) {
        return requestIds.contains(requestId);
    }

    public void addBid(Bid bid) {
        bids.add(bid);
        requestIds.add(bid.requestId());

        Bid existing = highestBidByBidder.get(bid.bidderId());
        if (existing == null || bid.amount() > existing.amount()) {
            highestBidByBidder.put(bid.bidderId(), bid);
        }
    }

    public Map<String, Long> highestBidByBidder() {
        Map<String, Long> result = new HashMap<>();
        highestBidByBidder.forEach((bidderId, bid) -> result.put(bidderId, bid.amount()));
        return result;
    }
}
