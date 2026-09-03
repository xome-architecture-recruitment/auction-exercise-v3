using AuctionBids.Models;
using AuctionBids.Store;
using Microsoft.AspNetCore.RateLimiting;
namespace AuctionBids.Services
{
    public class AuctionService : IAuctionService
    {
        private const decimal BidIncrement = 5000m;
        private readonly AuctionState _state;
        private readonly ILogger<AuctionService> _logger;
        private readonly object _lock = new object();

        public AuctionService(AuctionState state, ILogger<AuctionService> logger)
        {
            _state = state;
            _logger = logger;
        }

        public BidResponse PlaceBid(PlaceBidRequest request)
        {
            lock (_lock)
            {
                if (string.IsNullOrWhiteSpace(request.BidderId))
                {
                    return Rejected("BidderId is required");
                }
                if (string.IsNullOrWhiteSpace(request.RequestId))
                {
                    return Rejected("RequestId is required");
                }
                if (request.MaximumAmount <= 0)
                {
                    return Rejected("MaximumAmount must be greater than zero");
                }
                if (DateTime.UtcNow >= _state.ClosingTime)
                {
                    return Rejected("Auction is closed");
                }
                if (_state.ProcessedRequests.TryGetValue(request.RequestId, out object previousResult))
                {
                    return (BidResponse)previousResult;
                }
                decimal existingMaximum;
                if (_state.MaximunBids.TryGetValue(request.BidderId, out existingMaximum) && request.MaximumAmount <= existingMaximum)
                {
                    _state.ProcessedRequests[request.RequestId] = request.MaximumAmount;
                    return Rejected("The new maximum bid must be greater than the current maximum bid");
                }

                _state.MaximunBids[request.BidderId] = request.MaximumAmount;

                RecalculateAuction();

                return new BidResponse
                {
                    Accepted = true,
                    Message = "Bid accepted",
                    CurrentPrice = _state.CurrentPrice,
                    LeadingBidderId = _state.LeadingBidderId,
                    ReserveMet = _state.CurrentPrice >= _state.ReservePrice
                };
            }
        }

        public AuctionStatusResponse GetStatus()
        {
            lock (_state)
            {
                return new AuctionStatusResponse
                {
                    StartingPrice = _state.StartingPrice,
                    CurrentPrice = _state.CurrentPrice,
                    LeadingBidderId = _state.LeadingBidderId,
                    ReserveMet = _state.CurrentPrice >= _state.ReservePrice,
                    ClosingTime = _state.ClosingTime,
                    IsClosed = DateTime.UtcNow >= _state.ClosingTime
                };
            }
        }

        private void RecalculateAuction()
        {
            List<KeyValuePair<string, decimal>> sortedBidders = _state.MaximunBids.OrderByDescending(b => b.Value).ThenBy(b => b.Key).ToList();

            KeyValuePair<string, decimal> leadingBidder = sortedBidders[0];
            _state.LeadingBidderId = leadingBidder.Key;
            if (sortedBidders.Count == 1)
            {
                _state.CurrentPrice = _state.StartingPrice;
                return;
            }
            KeyValuePair<string, decimal> secondBidder = sortedBidders[1];
            _state.CurrentPrice = Math.Min(_state.StartingPrice, secondBidder.Value + BidIncrement);
        }
        private static BidResponse Rejected(string message)
        {
            return new BidResponse
            {
                Accepted = false,
                Message = message,
                CurrentPrice = 0,
                LeadingBidderId = null,
                ReserveMet = false
            };
        }
    }
}
