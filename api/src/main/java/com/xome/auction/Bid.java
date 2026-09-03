package com.xome.auction;

import java.time.Instant;

public record Bid(String requestId, String bidderId, long amount, Instant placedAt) {
}
